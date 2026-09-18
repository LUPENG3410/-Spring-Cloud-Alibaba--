package com.changxing.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.changxing.booking.dto.BookingCreateMessage;
import com.changxing.booking.dto.BookingCreateRequest;
import com.changxing.booking.dto.BookingDTO;
import com.changxing.booking.entity.Booking;
import com.changxing.booking.entity.InsuranceProduct;
import com.changxing.booking.mapper.BookingMapper;
import com.changxing.common.dto.Result;
import com.changxing.common.exception.BusinessException;
import com.changxing.common.feign.CarFeignClient;
import com.changxing.common.feign.StoreFeignClient;
import com.changxing.common.feign.UserFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingMapper bookingMapper;
    private final CarFeignClient carFeignClient;
    private final UserFeignClient userFeignClient;
    private final StoreFeignClient storeFeignClient;
    private final InsuranceProductService insuranceProductService;
    private final OccupancyService occupancyService;
    private final MessageProducerService messageProducerService;
    private final BookingPersistService bookingPersistService;

    public BookingDTO createBooking(BookingCreateRequest request, Long userId) {
        // 1. 校验参数
        validateRequest(request);

        Long occupancyTrimId = request.getTrimId() != null ? request.getTrimId() : request.getCarId();
        String province = request.getPickupProvince();

        // 2. Redis 原子日期段预占（替代全局分布式锁排队，抢车并发在此快速失败）
        if (!occupancyService.tryOccupy(occupancyTrimId, province, request.getStartDate(), request.getEndDate())) {
            throw new BusinessException("该车辆在该时间段已被预订");
        }

        try {
            // 3. 生成订单号
            String orderNo = generateOrderNo();

            // 4. 发 MQ 异步落库：请求线程到此结束，快速返回受理中（削峰）
            BookingCreateMessage msg = BookingCreateMessage.builder()
                    .orderNo(orderNo)
                    .userId(userId)
                    .request(request)
                    .build();
            messageProducerService.sendBookingCreateMessage(msg);

            // 5. 返回受理中结果
            return buildAcceptedDTO(request, userId, orderNo);
        } catch (RuntimeException e) {
            // 受理失败则回滚预占，避免占用泄漏
            occupancyService.release(occupancyTrimId, province, request.getStartDate(), request.getEndDate());
            throw e;
        }
    }

    /**
     * 下单真正落库（由 MQ 消费者调用）：事务只包 DB 写，远程取价在事务外，实现写请求削峰
     */
    public void processBookingCreate(BookingCreateRequest request, Long userId, String orderNo) {
        Long trimId = request.getTrimId() != null ? request.getTrimId() : request.getCarId();
        String province = request.getPickupProvince();

        // 幂等：消息重复投递时不重复下单
        Long exists = bookingMapper.selectCount(
                new LambdaQueryWrapper<Booking>().eq(Booking::getOrderNo, orderNo));
        if (exists != null && exists > 0) {
            log.warn("订单已存在，跳过落库: orderNo={}", orderNo);
            return;
        }

        // 事务外：远程取价 + 保险组装（读操作，不占事务/行锁）
        Booking booking = buildBooking(request, userId, trimId, orderNo);

        // 事务内：唯一兜底校验 + insert（短事务）
        bookingPersistService.createPending(booking, trimId, province);

        // 事务提交后：发送状态消息通知车辆服务
        messageProducerService.sendBookingStatusMessage(
                booking.getId(),
                booking.getOrderNo(),
                userId,
                request.getCarId(),
                booking.getTrimId(),
                "pending",
                request.getCarName(),
                booking.getPickupProvince()
        );
    }

    public List<BookingDTO> getUserBookings(Long userId) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getUserId, userId)
                .orderByDesc(Booking::getCreatedAt);

        List<Booking> bookings = bookingMapper.selectList(wrapper);

        return bookings.stream()
                .map(booking -> {
                    Map<String, Object> carInfo = null;
                    try {
                        Result<Map<String, Object>> carResult = carFeignClient.getCarById(booking.getCarId());
                        if (carResult != null && carResult.getCode() == 200) {
                            carInfo = carResult.getData();
                        }
                    } catch (Exception e) {
                        log.warn("获取车辆信息失败, carId={}: {}", booking.getCarId(), e.getMessage());
                    }
                    return convertToDTO(booking, carInfo);
                })
                .collect(Collectors.toList());
    }

    public BookingDTO getBookingById(Long id, Long userId) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new BusinessException("订单不存在");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此订单");
        }

        Map<String, Object> carInfo = null;
        try {
            Result<Map<String, Object>> carResult = carFeignClient.getCarById(booking.getCarId());
            if (carResult != null && carResult.getCode() == 200) {
                carInfo = carResult.getData();
            }
        } catch (Exception e) {
            log.warn("获取车辆信息失败, carId={}: {}", booking.getCarId(), e.getMessage());
        }

        return convertToDTO(booking, carInfo);
    }

    @Transactional
    public void cancelBooking(Long id, Long userId) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new BusinessException("订单不存在");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("无权取消此订单");
        }
        if (!"pending".equals(booking.getStatus()) && !"confirmed".equals(booking.getStatus())) {
            throw new BusinessException("当前订单状态不可取消");
        }

        booking.setStatus("cancelled");
        booking.setUpdatedAt(LocalDateTime.now());
        bookingMapper.updateById(booking);

        occupancyService.release(booking.getTrimId() != null ? booking.getTrimId() : booking.getCarId(),
                booking.getPickupProvince(), booking.getStartDate(), booking.getEndDate());
    }

    public void updateBookingStatus(Long id, String status) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new BusinessException("订单不存在");
        }

        booking.setStatus(status);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingMapper.updateById(booking);

        // 订单进入终态（取消/完成）时释放 Redis 占用
        if ("cancelled".equals(status) || "completed".equals(status)) {
            occupancyService.release(booking.getTrimId() != null ? booking.getTrimId() : booking.getCarId(),
                    booking.getPickupProvince(), booking.getStartDate(), booking.getEndDate());
        }

        // 发送订单状态变更消息
        messageProducerService.sendBookingStatusMessage(
                booking.getId(),
                booking.getOrderNo(),
                booking.getUserId(),
                booking.getCarId(),
                booking.getTrimId(),
                status,
                booking.getCarId() != null ? "车辆" + booking.getCarId() : "",
                booking.getPickupProvince()
        );

        // 订单完成时，发送完成消息并更新车辆位置
        if ("completed".equals(status)) {
            // 发送订单完成消息（通知用户服务更新积分）
            messageProducerService.sendBookingCompletedMessage(
                    booking.getId(),
                    booking.getOrderNo(),
                    booking.getUserId(),
                    booking.getCarId(),
                    booking.getTrimId(),
                    booking.getTotalDays(),
                    booking.getPickupProvince(),
                    booking.getReturnProvince()
            );

            // 异地还车时更新车辆位置
            if (booking.getPickupProvince() != null && booking.getReturnProvince() != null
                    && !booking.getPickupProvince().equals(booking.getReturnProvince())) {
                try {
                    Long trimId = booking.getTrimId() != null ? booking.getTrimId() : booking.getCarId();
                    carFeignClient.updateCarProvince(trimId, booking.getPickupProvince(), booking.getReturnProvince());
                    log.info("异地还车顺风车标记: trimId={}, {} -> {}", trimId, booking.getPickupProvince(), booking.getReturnProvince());
                } catch (Exception e) {
                    log.warn("更新顺风车省份失败: {}", e.getMessage());
                }
            }
        }
    }

    private void validateRequest(BookingCreateRequest request) {
        if (request.getCarId() == null) {
            throw new BusinessException("车辆ID不能为空");
        }
        if (request.getStartDate() == null) {
            throw new BusinessException("取车日期不能为空");
        }
        if (request.getEndDate() == null) {
            throw new BusinessException("还车日期不能为空");
        }
        if (request.getPickupTime() == null) {
            throw new BusinessException("取车时间不能为空");
        }
        if (request.getReturnTime() == null) {
            throw new BusinessException("还车时间不能为空");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException("取车日期不能晚于还车日期");
        }
        if (request.getStartDate().isEqual(request.getEndDate()) && request.getPickupTime().isAfter(request.getReturnTime())) {
            throw new BusinessException("同一天取车时间不能晚于还车时间");
        }
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BusinessException("取车日期不能早于今天");
        }
    }

    public boolean checkAvailability(Long trimId, String pickupProvince, String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        return bookingPersistService.isAvailable(trimId, pickupProvince, startDate, endDate);
    }

    public List<Long> getActiveTrimIds() {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Booking::getStatus, "pending", "confirmed", "active")
                .select(Booking::getTrimId)
                .groupBy(Booking::getTrimId);
        return bookingMapper.selectList(wrapper).stream()
                .map(Booking::getTrimId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    private Booking buildBooking(BookingCreateRequest request, Long userId, Long trimId, String orderNo) {
        // 计算租赁天数和价格（优先使用前端传入的值）
        int totalDays;
        if (request.getPickupTime() != null && request.getReturnTime() != null) {
            long hours = ChronoUnit.HOURS.between(request.getPickupTime(), request.getReturnTime());
            totalDays = (int) Math.max(1, Math.ceil(hours / 24.0));
        } else {
            totalDays = (int) (ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1);
        }
        BigDecimal dailyPrice = BigDecimal.ZERO;
        BigDecimal totalPrice = request.getTotalPrice() != null ? request.getTotalPrice() : BigDecimal.ZERO;

        // 前端未传价格时，从车辆服务远程获取
        if (totalPrice.compareTo(BigDecimal.ZERO) == 0) {
            Result<Map<String, Object>> carResult = carFeignClient.getCarById(request.getCarId());
            if (carResult != null && carResult.getCode() == 200 && carResult.getData() != null) {
                Map<String, Object> carInfo = carResult.getData();
                if (carInfo.get("rentalPrice") != null) {
                    dailyPrice = new BigDecimal(carInfo.get("rentalPrice").toString());
                    totalPrice = dailyPrice.multiply(BigDecimal.valueOf(totalDays));
                }
            }
        }

        Booking booking = new Booking();
        booking.setOrderNo(orderNo);
        booking.setUserId(userId);
        booking.setCarId(request.getCarId());
        booking.setTrimId(trimId);
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setPickupTime(request.getPickupTime());
        booking.setReturnTime(request.getReturnTime());
        booking.setTotalDays(totalDays);
        booking.setDailyPrice(dailyPrice);
        booking.setTotalPrice(totalPrice);
        booking.setPickupStoreId(request.getPickupStoreId() != null ? request.getPickupStoreId() : 0L);
        booking.setReturnStoreId(request.getReturnStoreId() != null ? request.getReturnStoreId() : 0L);
        booking.setPickupProvince(request.getPickupProvince() != null ? request.getPickupProvince() : "");
        booking.setReturnProvince(request.getReturnProvince() != null ? request.getReturnProvince() : "");
        booking.setStatus("pending");
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        // 处理保险产品
        InsuranceProduct insuranceProduct = null;
        if (request.getInsuranceProductId() != null) {
            insuranceProduct = insuranceProductService.getById(request.getInsuranceProductId());
            if (insuranceProduct != null && insuranceProduct.getStatus() == 1) {
                booking.setInsuranceProductId(insuranceProduct.getId());
                booking.setInsurancePrice(insuranceProduct.getTotalPrice());
                booking.setInsuranceCode(insuranceProduct.getCode());
            }
        } else {
            insuranceProduct = insuranceProductService.getByCode("basic");
            if (insuranceProduct != null) {
                booking.setInsuranceProductId(insuranceProduct.getId());
                booking.setInsurancePrice(BigDecimal.ZERO);
                booking.setInsuranceCode("basic");
            }
        }
        return booking;
    }

    private BookingDTO buildAcceptedDTO(BookingCreateRequest request, Long userId, String orderNo) {
        BookingDTO dto = new BookingDTO();
        dto.setOrderNo(orderNo);
        dto.setUserId(userId);
        dto.setCarId(request.getCarId());
        dto.setCarName(request.getCarName());
        dto.setCarImage(request.getCarImage());
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setPickupTime(request.getPickupTime());
        dto.setReturnTime(request.getReturnTime());
        dto.setPickupLocation(request.getPickupLocation());
        dto.setReturnLocation(request.getReturnLocation());
        dto.setPickupProvince(request.getPickupProvince());
        dto.setReturnProvince(request.getReturnProvince());
        dto.setStatus("pending");
        return dto;
    }

    private String generateOrderNo() {
        return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private BookingDTO convertToDTO(Booking booking, Map<String, Object> carInfo) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setOrderNo(booking.getOrderNo());
        dto.setUserId(booking.getUserId());
        dto.setCarId(booking.getCarId());
        dto.setDailyPrice(booking.getDailyPrice());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setPickupTime(booking.getPickupTime());
        dto.setReturnTime(booking.getReturnTime());
        dto.setTotalDays(booking.getTotalDays());
        dto.setPickupProvince(booking.getPickupProvince());
        dto.setReturnProvince(booking.getReturnProvince());
        dto.setStatus(booking.getStatus());
        dto.setInsuranceProductId(booking.getInsuranceProductId());
        dto.setInsurancePrice(booking.getInsurancePrice());
        dto.setCreatedAt(booking.getCreatedAt());

        // 查询保险产品信息
        if (booking.getInsuranceProductId() != null) {
            try {
                InsuranceProduct insuranceProduct = insuranceProductService.getById(booking.getInsuranceProductId());
                if (insuranceProduct != null) {
                    dto.setInsuranceName(insuranceProduct.getName());
                }
            } catch (Exception e) {
                log.warn("获取保险产品信息失败, insuranceProductId={}: {}", booking.getInsuranceProductId(), e.getMessage());
            }
        }

        if (carInfo != null) {
            dto.setCarName((String) carInfo.get("name"));
            dto.setCarImage((String) carInfo.get("image"));
        }

        // 查询用户信息
        try {
            Result<Map<String, Object>> userResult = userFeignClient.getUserById(booking.getUserId());
            if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                dto.setUserName((String) userResult.getData().get("name"));
                dto.setUserPhone((String) userResult.getData().get("phone"));
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败, userId={}: {}", booking.getUserId(), e.getMessage());
        }

        // 查询门店信息
        if (booking.getPickupStoreId() != null && booking.getPickupStoreId() > 0) {
            try {
                Result<Map<String, Object>> storeResult = storeFeignClient.getStoreById(booking.getPickupStoreId());
                if (storeResult != null && storeResult.getCode() == 200 && storeResult.getData() != null) {
                    dto.setPickupLocation((String) storeResult.getData().get("name"));
                }
            } catch (Exception e) {
                log.warn("获取取车门店信息失败, storeId={}: {}", booking.getPickupStoreId(), e.getMessage());
            }
        }
        if (booking.getReturnStoreId() != null && booking.getReturnStoreId() > 0) {
            try {
                Result<Map<String, Object>> storeResult = storeFeignClient.getStoreById(booking.getReturnStoreId());
                if (storeResult != null && storeResult.getCode() == 200 && storeResult.getData() != null) {
                    dto.setReturnLocation((String) storeResult.getData().get("name"));
                }
            } catch (Exception e) {
                log.warn("获取还车门店信息失败, storeId={}: {}", booking.getReturnStoreId(), e.getMessage());
            }
        }

        return dto;
    }
}