package com.changxing.car.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.changxing.car.cache.TwoLevelCache;
import com.changxing.car.dto.CarDTO;
import com.changxing.car.dto.TrimDetailDTO;
import com.changxing.car.entity.Car;
import com.changxing.car.mapper.CarMapper;
import com.changxing.car.mapper.TrimMapper;
import com.changxing.common.dto.Result;
import com.changxing.common.feign.ReviewFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarService {

    private final TrimMapper trimMapper;
    private final CarMapper carMapper;
    private final ReviewFeignClient reviewFeignClient;
    private final TwoLevelCache twoLevelCache;

    private static final String KEY_CAR_LIST = "carList";
    private static final String KEY_HITCH = "hitchList";
    private static final String KEY_HOT = "hotTrims";

    public List<CarDTO> getHasStockCarList(String type, String province, String keyword) {
        String key = KEY_CAR_LIST + ":" + type + ":" + province + ":" + keyword;
        return twoLevelCache.getList(key, CarDTO.class, 300,
                () -> loadHasStockCarList(type, province, keyword));
    }

    private List<CarDTO> loadHasStockCarList(String type, String province, String keyword) {
        List<CarDTO> dtoList = trimMapper.listHasStockTrim(type, province, keyword);
        if (CollectionUtils.isEmpty(dtoList)) {
            return dtoList;
        }

        for (CarDTO dto : dtoList) {
            String featureJson = Objects.toString(dto.getFeatures(), "");
            if (StringUtils.hasText(featureJson)) {
                List<String> featureList = JSON.parseArray(featureJson, String.class);
                dto.setFeatures(featureList);
            } else {
                dto.setFeatures(List.of());
            }
            // isHitch 由 SQL CASE 返回 0/1，转为 Boolean
            dto.setIsHitch(Boolean.TRUE.equals(dto.getIsHitch()));
        }
        return dtoList;
    }

    public List<CarDTO> getHitchCarList() {
        return twoLevelCache.getList(KEY_HITCH, CarDTO.class, 300,
                this::loadHitchCars);
    }

    private List<CarDTO> loadHitchCars() {
        List<CarDTO> dtoList = trimMapper.listHitchCars();
        if (CollectionUtils.isEmpty(dtoList)) {
            return dtoList;
        }

        for (CarDTO dto : dtoList) {
            String featureJson = Objects.toString(dto.getFeatures(), "");
            if (StringUtils.hasText(featureJson)) {
                List<String> featureList = JSON.parseArray(featureJson, String.class);
                dto.setFeatures(featureList);
            } else {
                dto.setFeatures(List.of());
            }
            dto.setIsHitch(true);
        }
        return dtoList;
    }

    public void updateCarProvince(Long trimId, String fromProvince, String toProvince) {
        // 找到当前在 fromProvince 的那辆车，把它改为 toProvince
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getTrimId, trimId)
                .eq(Car::getStatus, "available")
                .eq(Car::getProvince, fromProvince);
        wrapper.last("LIMIT 1");

        Car car = carMapper.selectOne(wrapper);
        if (car != null) {
            car.setProvince(toProvince);
            carMapper.updateById(car);
            log.info("顺风车更新: trimId={}, plate={}, {} -> {}", trimId, car.getPlateNumber(), fromProvince, toProvince);
            twoLevelCache.evictByPrefix(KEY_CAR_LIST + ":");
            twoLevelCache.evict(KEY_HITCH);
            twoLevelCache.evict(KEY_HOT);
        } else {
            log.warn("未找到顺风车: trimId={}, province={}", trimId, fromProvince);
        }
    }

    public void updateCarStatus(Long trimId, String status) {
        // 订单状态变更时同步车辆状态（车辆表以 trimId 关联，取一辆）
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getTrimId, trimId);
        wrapper.last("LIMIT 1");
        Car car = carMapper.selectOne(wrapper);
        if (car == null) {
            return;
        }
        car.setStatus(status);
        carMapper.updateById(car);

        twoLevelCache.evictByPrefix(KEY_CAR_LIST + ":");
        twoLevelCache.evict(KEY_HITCH);
        twoLevelCache.evict(KEY_HOT);
    }

    public TrimDetailDTO getTrimDetail(Long trimId) {
        TrimDetailDTO dto = trimMapper.getTrimDetail(trimId);
        if (dto == null) {
            return null;
        }

        dto.setFeatures(parseJsonList(dto.getFeaturesStr()));
        dto.setImages(parseJsonList(dto.getImagesStr()));
        dto.setInteriorImages(parseJsonList(dto.getInteriorImagesStr()));
        dto.setDescriptions(parseJsonList(dto.getDescriptionsStr()));

        // 从review服务获取真实评价
        try {
            Result<List<Map<String, Object>>> reviewResult = reviewFeignClient.getReviewsByTrimId(trimId);
            if (reviewResult != null && reviewResult.getCode() == 200 && reviewResult.getData() != null) {
                List<Map<String, Object>> reviews = reviewResult.getData().stream()
                        .map(r -> {
                            Map<String, Object> item = new java.util.HashMap<>();
                            item.put("id", r.get("id"));
                            item.put("userName", r.get("userName"));
                            item.put("avatar", r.get("avatar"));
                            item.put("rating", r.get("rating"));
                            item.put("content", r.get("content"));
                            item.put("carDays", r.get("carDays"));
                            Object createdAt = r.get("createdAt");
                            if (createdAt != null) {
                                String dateStr = createdAt.toString().substring(0, 10);
                                item.put("date", dateStr);
                            }
                            return item;
                        })
                        .collect(java.util.stream.Collectors.toList());
                dto.setReviews(reviews);
            }
        } catch (Exception e) {
            log.warn("获取评价失败, trimId={}: {}", trimId, e.getMessage());
            dto.setReviews(List.of());
        }

        return dto;
    }

    public List<TrimDetailDTO> getHotTrims() {
        return twoLevelCache.getList(KEY_HOT, TrimDetailDTO.class, 300,
                this::loadHotTrims);
    }

    private List<TrimDetailDTO> loadHotTrims() {
        List<TrimDetailDTO> list = trimMapper.listHotTrims();
        if (list == null) return List.of();
        for (TrimDetailDTO dto : list) {
            dto.setFeatures(parseJsonList(dto.getFeaturesStr()));
            dto.setImages(parseJsonList(dto.getImagesStr()));
            dto.setInteriorImages(parseJsonList(dto.getInteriorImagesStr()));
            dto.setDescriptions(parseJsonList(dto.getDescriptionsStr()));
        }
        return list;
    }

    private List<String> parseJsonList(String json) {
        if (StringUtils.hasText(json)) {
            return JSON.parseArray(json, String.class);
        }
        return List.of();
    }
}