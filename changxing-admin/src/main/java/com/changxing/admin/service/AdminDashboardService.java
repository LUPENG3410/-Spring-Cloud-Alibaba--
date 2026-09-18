package com.changxing.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.changxing.admin.dto.*;
import com.changxing.admin.entity.*;
import com.changxing.admin.mapper.*;
import com.changxing.common.exception.BusinessException;
import com.changxing.common.feign.BookingFeignClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final AdminMapper adminMapper;
    private final CarMapper carMapper;
    private final CarTrimMapper carTrimMapper;
    private final UserMapper userMapper;
    private final StoreMapper storeMapper;
    private final BookingMapper bookingMapper;
    private final ObjectMapper objectMapper;
    private final BookingFeignClient bookingFeignClient;

    public AdminStatsDTO getStats() {
        AdminStatsDTO stats = new AdminStatsDTO();
        stats.setTotalCars(carMapper.selectCount(null));
        stats.setAvailableCars(carMapper.selectCount(
                new LambdaQueryWrapper<Car>().eq(Car::getStatus, "available")));
        stats.setTotalBookings(bookingMapper.selectCount(null));
        stats.setPendingBookings(bookingMapper.selectCount(
                new LambdaQueryWrapper<Booking>().eq(Booking::getStatus, "pending")));
        stats.setTotalUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().isNull(User::getDeletedAt)));
        stats.setTotalStores(storeMapper.selectCount(
                new LambdaQueryWrapper<Store>().eq(Store::getStatus, 1)));

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        List<Booking> monthBookings = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getStatus, "completed")
                        .ge(Booking::getStartDate, monthStart));
        long revenue = monthBookings.stream()
                .mapToLong(b -> b.getTotalPrice() != null ? b.getTotalPrice().longValue() : 0)
                .sum();
        stats.setMonthlyRevenue(revenue);

        LocalDate today = LocalDate.now();
        stats.setTodayBookings(bookingMapper.selectCount(
                new LambdaQueryWrapper<Booking>()
                        .ge(Booking::getCreatedAt, today.atStartOfDay())));

        return stats;
    }

    public List<BookingDTO> getBookings() {
        List<Booking> bookings = bookingMapper.selectList(null);
        List<BookingDTO> result = new ArrayList<>();
        for (Booking b : bookings) {
            BookingDTO dto = new BookingDTO();
            dto.setId(b.getId());
            dto.setUserId(b.getUserId());
            dto.setCarId(b.getCarId());
            if (b.getStartDate() != null) dto.setStartDate(b.getStartDate().toString());
            if (b.getEndDate() != null) dto.setEndDate(b.getEndDate().toString());
            if (b.getPickupTime() != null) dto.setPickupTime(b.getPickupTime().toString());
            if (b.getReturnTime() != null) dto.setReturnTime(b.getReturnTime().toString());
            dto.setTotalDays(b.getTotalDays());
            dto.setTotalPrice(b.getTotalPrice() != null ? b.getTotalPrice().intValue() : 0);
            dto.setStatus(b.getStatus());
            if (b.getPickupStoreId() != null) {
                Store s = storeMapper.selectById(b.getPickupStoreId());
                dto.setPickupLocation(s != null ? s.getName() : "");
            }
            if (b.getReturnStoreId() != null) {
                Store s = storeMapper.selectById(b.getReturnStoreId());
                dto.setReturnLocation(s != null ? s.getName() : "");
            }
            dto.setPickupProvince(b.getPickupProvince());
            dto.setReturnProvince(b.getReturnProvince());
            dto.setInsuranceProductId(b.getInsuranceProductId());
            dto.setInsuranceName(getInsuranceName(b.getInsuranceCode()));
            dto.setInsurancePrice(b.getInsurancePrice() != null ? b.getInsurancePrice().intValue() : 0);
            if (b.getCreatedAt() != null) {
                dto.setCreatedAt(b.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            User user = userMapper.selectById(b.getUserId());
            if (user != null) {
                dto.setUserName(user.getName());
                dto.setUserPhone(user.getPhone());
            }

            String defaultImg = "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=400&h=250&fit=crop";
            CarTrim trim = null;
            Car car = carMapper.selectById(b.getCarId());
            if (car != null) {
                trim = carTrimMapper.selectById(car.getTrimId());
            }
            if (trim == null && b.getTrimId() != null) {
                trim = carTrimMapper.selectById(b.getTrimId());
            }
            if (trim != null) {
                dto.setCarName(trim.getName());
                dto.setCarImage(trim.getImage() != null ? trim.getImage() : defaultImg);
            }

            if (b.getPickupStoreId() != null && b.getPickupStoreId() > 0) {
                Store s = storeMapper.selectById(b.getPickupStoreId());
                dto.setPickupLocation(s != null ? s.getName() : "");
            }
            if (b.getReturnStoreId() != null && b.getReturnStoreId() > 0) {
                Store s = storeMapper.selectById(b.getReturnStoreId());
                dto.setReturnLocation(s != null ? s.getName() : "");
            }

            result.add(dto);
        }
        return result;
    }

    public void updateBookingStatus(Long id, String status) {
        // 通过 booking 服务更新状态（触发顺风车逻辑）
        try {
            bookingFeignClient.updateBookingStatus(id, status);
        } catch (Exception e) {
            log.warn("通过Feign更新订单状态失败，直接更新数据库: {}", e.getMessage());
            Booking booking = bookingMapper.selectById(id);
            if (booking != null) {
                booking.setStatus(status);
                bookingMapper.updateById(booking);
            }
        }
    }

    public List<UserDTO> getUsers() {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().isNull(User::getDeletedAt));
        List<UserDTO> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (User u : users) {
            UserDTO dto = new UserDTO();
            dto.setId(u.getId());
            dto.setName(u.getName());
            dto.setPhone(u.getPhone());
            dto.setAvatar(u.getAvatar());
            dto.setMemberLevel(u.getMemberLevel());
            dto.setStatus(u.getStatus() != null && u.getStatus() == 1 ? "active" : "disabled");
            if (u.getCreatedAt() != null) {
                dto.setRegisterDate(u.getCreatedAt().format(fmt));
            }
            long orderCount = bookingMapper.selectCount(
                    new LambdaQueryWrapper<Booking>().eq(Booking::getUserId, u.getId()));
            dto.setTotalOrders(orderCount);
            List<Booking> userBookings = bookingMapper.selectList(
                    new LambdaQueryWrapper<Booking>()
                            .eq(Booking::getUserId, u.getId())
                            .eq(Booking::getStatus, "completed"));
            long spent = userBookings.stream()
                    .mapToLong(b -> b.getTotalPrice() != null ? b.getTotalPrice().longValue() : 0)
                    .sum();
            dto.setTotalSpent(spent);
            result.add(dto);
        }
        return result;
    }

    public void updateUserStatus(Long id, String status) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus("active".equals(status) ? 1 : 0);
            userMapper.updateById(user);
        }
    }

    public List<TrimDTO> getTrims() {
        List<CarTrim> trims = carTrimMapper.selectList(null);
        List<TrimDTO> result = new ArrayList<>();
        for (CarTrim t : trims) {
            result.add(convertToTrimDTO(t));
        }
        return result;
    }

    public TrimDTO createTrim(CreateTrimRequest req) {
        CarTrim trim = new CarTrim();
        trim.setBrand(req.getBrand());
        trim.setSeriesName(req.getSeriesName());
        trim.setName(req.getName());
        trim.setType(req.getType());
        trim.setYear(req.getYear());
        trim.setImage(req.getImage());
        if (req.getPrice() != null) trim.setPrice(java.math.BigDecimal.valueOf(req.getPrice()));
        if (req.getRentalPrice() != null) trim.setRentalPrice(java.math.BigDecimal.valueOf(req.getRentalPrice()));
        trim.setSeats(req.getSeats());
        trim.setFuel(req.getFuel());
        trim.setTransmission(req.getTransmission());
        trim.setEngine(req.getEngine());
        trim.setHorsepower(req.getHorsepower());
        trim.setTorque(req.getTorque());
        trim.setDisplacement(req.getDisplacement());
        trim.setAcceleration(req.getAcceleration());
        trim.setTopSpeed(req.getTopSpeed());
        trim.setFuelConsumption(req.getFuelConsumption());
        trim.setWheelbase(req.getWheelbase());
        trim.setLength(req.getLength());
        trim.setWidth(req.getWidth());
        trim.setHeight(req.getHeight());
        trim.setTrunkVolume(req.getTrunkVolume());
        trim.setWeight(req.getWeight());
        trim.setFuelType(req.getFuelType());
        trim.setFuelGrade(req.getFuelGrade());
        trim.setFuelTankCapacity(req.getFuelTankCapacity());
        trim.setDoors(req.getDoors());
        trim.setReversingCamera(Boolean.TRUE.equals(req.getReversingCamera()) ? 1 : 0);
        trim.setAutoHold(Boolean.TRUE.equals(req.getAutoHold()) ? 1 : 0);
        trim.setDriverAssist(req.getDriverAssist());
        trim.setSmartConnect(req.getSmartConnect());
        trim.setElectricSeat(Boolean.TRUE.equals(req.getElectricSeat()) ? 1 : 0);
        trim.setSeatFunction(req.getSeatFunction());
        trim.setImages(toJson(req.getImages()));
        trim.setInteriorImages(toJson(req.getInteriorImages()));
        trim.setFeatures(toJson(req.getFeatures()));
        trim.setDescriptions(toJson(req.getDescriptions()));
        trim.setStatus(1);
        carTrimMapper.insert(trim);
        return convertToTrimDTO(trim);
    }

    public void updateTrimStatus(Long id, Integer status) {
        CarTrim trim = carTrimMapper.selectById(id);
        if (trim != null) {
            trim.setStatus(status);
            carTrimMapper.updateById(trim);
        }
    }

    public void deleteTrim(Long id) {
        long carCount = carMapper.selectCount(
                new LambdaQueryWrapper<Car>().eq(Car::getTrimId, id));
        if (carCount > 0) {
            throw new com.changxing.common.exception.BusinessException("该车款下仍有库存车辆，无法删除");
        }
        carTrimMapper.deleteById(id);
    }

    public CarDTO createCar(CreateCarRequest req) {
        Store store = storeMapper.selectById(req.getStoreId());
        if (store == null) {
            throw new BusinessException("门店不存在");
        }

        CarTrim trim = new CarTrim();
        trim.setBrand(req.getBrand());
        trim.setSeriesName(req.getName());
        trim.setName(req.getName());
        trim.setType(req.getType());
        if (req.getYear() != null) trim.setYear(req.getYear());
        trim.setImage(req.getImage());
        if (req.getPrice() != null) trim.setPrice(java.math.BigDecimal.valueOf(req.getPrice()));
        if (req.getPrice() != null) trim.setRentalPrice(java.math.BigDecimal.valueOf(req.getPrice()));
        if (req.getSeats() != null) trim.setSeats(req.getSeats());
        trim.setFuel(req.getFuel());
        trim.setTransmission(req.getTransmission());
        trim.setEngine(req.getEngine());
        if (req.getHorsepower() != null) trim.setHorsepower(req.getHorsepower());
        if (req.getTorque() != null) trim.setTorque(req.getTorque());
        trim.setDisplacement(req.getDisplacement());
        trim.setAcceleration(req.getAcceleration());
        trim.setTopSpeed(req.getTopSpeed());
        trim.setFuelConsumption(req.getFuelConsumption());
        trim.setWheelbase(req.getWheelbase());
        trim.setLength(req.getLength());
        trim.setWidth(req.getWidth());
        trim.setHeight(req.getHeight());
        trim.setTrunkVolume(req.getTrunkVolume());
        trim.setWeight(req.getWeight());
        trim.setFuelType(req.getFuelType());
        trim.setFuelGrade(req.getFuelGrade());
        trim.setFuelTankCapacity(req.getFuelTankCapacity());
        trim.setDoors(req.getDoors());
        trim.setReversingCamera(Boolean.TRUE.equals(req.getReversingCamera()) ? 1 : 0);
        trim.setAutoHold(Boolean.TRUE.equals(req.getAutoHold()) ? 1 : 0);
        trim.setDriverAssist(req.getDriverAssist());
        trim.setSmartConnect(req.getSmartConnect());
        trim.setElectricSeat(Boolean.TRUE.equals(req.getElectricSeat()) ? 1 : 0);
        trim.setSeatFunction(req.getSeatFunction());
        trim.setImages(toJson(req.getImages()));
        trim.setInteriorImages(toJson(req.getInteriorImages()));
        trim.setFeatures(toJson(req.getFeatures()));
        trim.setDescriptions(toJson(req.getDescriptions()));
        trim.setStatus(1);
        carTrimMapper.insert(trim);

        Car car = new Car();
        car.setTrimId(trim.getId());
        car.setStoreId(req.getStoreId());
        String plateNumber = buildPlateNumber(req.getPlateProvince(), req.getPlateLetter(), req.getPlateNumber());
        if (plateNumber != null) {
            long plateCount = carMapper.selectCount(
                    new LambdaQueryWrapper<Car>().eq(Car::getPlateNumber, plateNumber));
            if (plateCount > 0) {
                throw new BusinessException("车牌号已存在");
            }
        }
        car.setPlateNumber(plateNumber);
        car.setProvince(store.getProvince());
        car.setCity(store.getCity());
        car.setMileage(req.getMileage() != null ? req.getMileage() : 0);
        if (req.getLastMaintenance() != null && !req.getLastMaintenance().isBlank()) {
            car.setLastMaintenance(java.time.LocalDate.parse(req.getLastMaintenance()));
        }
        car.setStatus(req.getStatus() != null ? req.getStatus() : "available");
        carMapper.insert(car);

        return convertCarToDTO(car, trim, store);
    }

    private String generatePlateNumber(String province) {
        String prefix = province != null && !province.isBlank() ? province.charAt(0) + "" : "京";
        String letters = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String letter = String.valueOf(letters.charAt((int) (Math.random() * letters.length())));
        String digits = String.format("%05d", (int) (Math.random() * 100000));
        return prefix + letter + digits;
    }

    private String buildPlateNumber(String province, String letter, String number) {
        if (province != null && letter != null && number != null
                && !province.isBlank() && !letter.isBlank() && !number.isBlank()) {
            return province + letter.toUpperCase() + number;
        }
        return null;
    }

    public CarDTO createCarInventory(CreateCarInventoryRequest req) {
        CarTrim trim = carTrimMapper.selectById(req.getTrimId());
        if (trim == null) {
            throw new BusinessException("车款不存在");
        }
        Store store = storeMapper.selectById(req.getStoreId());
        if (store == null) {
            throw new BusinessException("门店不存在");
        }

        Car car = new Car();
        car.setTrimId(req.getTrimId());
        car.setStoreId(req.getStoreId());
        String plateNumber = buildPlateNumber(req.getPlateProvince(), req.getPlateLetter(), req.getPlateNumber());
        if (plateNumber == null) {
            plateNumber = generatePlateNumber(store.getProvince());
        }
        if (plateNumber != null) {
            long plateCount = carMapper.selectCount(
                    new LambdaQueryWrapper<Car>().eq(Car::getPlateNumber, plateNumber));
            if (plateCount > 0) {
                throw new BusinessException("车牌号已存在");
            }
        }
        car.setPlateNumber(plateNumber);
        car.setProvince(store.getProvince());
        car.setCity(store.getCity());
        car.setMileage(req.getMileage() != null ? req.getMileage() : 0);
        if (req.getLastMaintenance() != null && !req.getLastMaintenance().isBlank()) {
            car.setLastMaintenance(java.time.LocalDate.parse(req.getLastMaintenance()));
        }
        car.setStatus(req.getStatus() != null ? req.getStatus() : "available");
        carMapper.insert(car);

        return convertCarToDTO(car, trim, store);
    }

    private CarDTO convertCarToDTO(Car c, CarTrim trim, Store store) {
        CarDTO dto = new CarDTO();
        dto.setId(c.getId());
        dto.setTrimId(c.getTrimId());
        dto.setProvince(c.getProvince());
        dto.setStoreId(c.getStoreId());
        dto.setMileage(c.getMileage());
        dto.setPlateNumber(c.getPlateNumber());
        if (c.getLastMaintenance() != null) {
            dto.setLastMaintenance(c.getLastMaintenance().toString());
        }
        dto.setStatus(c.getStatus());
        if (trim != null) {
            dto.setName(trim.getName());
            dto.setBrand(trim.getBrand());
            dto.setType(trim.getType());
            dto.setImage(trim.getImage());
            dto.setPrice(trim.getRentalPrice() != null ? trim.getRentalPrice().intValue() : 0);
            dto.setSeats(trim.getSeats());
            dto.setFuel(trim.getFuel());
            dto.setTransmission(trim.getTransmission());
            dto.setAvailable("available".equals(c.getStatus()));
            dto.setYear(trim.getYear());
            dto.setEngine(trim.getEngine());
            dto.setHorsepower(trim.getHorsepower());
            dto.setTorque(trim.getTorque());
            dto.setDisplacement(trim.getDisplacement());
            dto.setAcceleration(trim.getAcceleration());
            dto.setTopSpeed(trim.getTopSpeed());
            dto.setFuelConsumption(trim.getFuelConsumption());
            dto.setWheelbase(trim.getWheelbase());
            dto.setLength(trim.getLength());
            dto.setWidth(trim.getWidth());
            dto.setHeight(trim.getHeight());
            dto.setTrunkVolume(trim.getTrunkVolume());
            dto.setWeight(trim.getWeight());
            dto.setFuelType(trim.getFuelType());
            dto.setFuelGrade(trim.getFuelGrade());
            dto.setFuelTankCapacity(trim.getFuelTankCapacity());
            dto.setDoors(trim.getDoors());
            dto.setReversingCamera(trim.getReversingCamera() != null && trim.getReversingCamera() == 1);
            dto.setAutoHold(trim.getAutoHold() != null && trim.getAutoHold() == 1);
            dto.setDriverAssist(trim.getDriverAssist());
            dto.setSmartConnect(trim.getSmartConnect());
            dto.setElectricSeat(trim.getElectricSeat() != null && trim.getElectricSeat() == 1);
            dto.setSeatFunction(trim.getSeatFunction());
            try {
                dto.setFeatures(parseList(trim.getFeatures()));
                dto.setImages(parseList(trim.getImages()));
                dto.setInteriorImages(parseList(trim.getInteriorImages()));
                dto.setDescriptions(parseList(trim.getDescriptions()));
            } catch (Exception e) {
                dto.setFeatures(List.of());
                dto.setImages(List.of());
                dto.setInteriorImages(List.of());
                dto.setDescriptions(List.of());
            }
        }
        if (store != null) {
            dto.setStoreName(store.getName());
        }
        return dto;
    }

    private TrimDTO convertToTrimDTO(CarTrim t) {
        TrimDTO dto = new TrimDTO();
        dto.setId(t.getId());
        dto.setBrand(t.getBrand());
        dto.setSeriesName(t.getSeriesName());
        dto.setName(t.getName());
        dto.setType(t.getType());
        dto.setYear(t.getYear());
        dto.setImage(t.getImage());
        dto.setPrice(t.getPrice() != null ? t.getPrice().doubleValue() : 0);
        dto.setRentalPrice(t.getRentalPrice() != null ? t.getRentalPrice().doubleValue() : 0);
        dto.setSeats(t.getSeats());
        dto.setFuel(t.getFuel());
        dto.setTransmission(t.getTransmission());
        dto.setEngine(t.getEngine());
        dto.setHorsepower(t.getHorsepower());
        dto.setTorque(t.getTorque());
        dto.setDisplacement(t.getDisplacement());
        dto.setAcceleration(t.getAcceleration());
        dto.setTopSpeed(t.getTopSpeed());
        dto.setFuelConsumption(t.getFuelConsumption());
        dto.setWheelbase(t.getWheelbase());
        dto.setLength(t.getLength());
        dto.setWidth(t.getWidth());
        dto.setHeight(t.getHeight());
        dto.setTrunkVolume(t.getTrunkVolume());
        dto.setWeight(t.getWeight());
        dto.setFuelType(t.getFuelType());
        dto.setFuelGrade(t.getFuelGrade());
        dto.setFuelTankCapacity(t.getFuelTankCapacity());
        dto.setDoors(t.getDoors());
        dto.setReversingCamera(t.getReversingCamera() != null && t.getReversingCamera() == 1);
        dto.setAutoHold(t.getAutoHold() != null && t.getAutoHold() == 1);
        dto.setDriverAssist(t.getDriverAssist());
        dto.setSmartConnect(t.getSmartConnect());
        dto.setElectricSeat(t.getElectricSeat() != null && t.getElectricSeat() == 1);
        dto.setSeatFunction(t.getSeatFunction());
        dto.setStatus(t.getStatus());
        try {
            dto.setFeatures(parseList(t.getFeatures()));
            dto.setImages(parseList(t.getImages()));
            dto.setInteriorImages(parseList(t.getInteriorImages()));
            dto.setDescriptions(parseList(t.getDescriptions()));
        } catch (Exception e) {
            dto.setFeatures(List.of());
            dto.setImages(List.of());
            dto.setInteriorImages(List.of());
            dto.setDescriptions(List.of());
        }
        long stockCount = carMapper.selectCount(
                new LambdaQueryWrapper<Car>().eq(Car::getTrimId, t.getId()));
        dto.setStockCount(stockCount);
        return dto;
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    public List<CarDTO> getCars() {
        List<Car> cars = carMapper.selectList(null);
        List<CarDTO> result = new ArrayList<>();
        for (Car c : cars) {
            CarDTO dto = new CarDTO();
            dto.setId(c.getId());
            dto.setTrimId(c.getTrimId());
            dto.setProvince(c.getProvince());
            dto.setStoreId(c.getStoreId());
            dto.setPlateNumber(c.getPlateNumber());
            dto.setMileage(c.getMileage());
            if (c.getLastMaintenance() != null) {
                dto.setLastMaintenance(c.getLastMaintenance().toString());
            }
            dto.setStatus(c.getStatus());

            CarTrim trim = carTrimMapper.selectById(c.getTrimId());
            if (trim != null) {
                dto.setName(trim.getName());
                dto.setBrand(trim.getBrand());
                dto.setType(trim.getType());
                dto.setImage(trim.getImage());
                dto.setPrice(trim.getRentalPrice() != null ? trim.getRentalPrice().intValue() : 0);
                dto.setSeats(trim.getSeats());
                dto.setFuel(trim.getFuel());
                dto.setTransmission(trim.getTransmission());
                dto.setAvailable("available".equals(c.getStatus()));
                dto.setYear(trim.getYear());
                dto.setEngine(trim.getEngine());
                dto.setHorsepower(trim.getHorsepower());
                dto.setTorque(trim.getTorque());
                dto.setDisplacement(trim.getDisplacement());
                dto.setAcceleration(trim.getAcceleration());
                dto.setTopSpeed(trim.getTopSpeed());
                dto.setFuelConsumption(trim.getFuelConsumption());
                dto.setWheelbase(trim.getWheelbase());
                dto.setLength(trim.getLength());
                dto.setWidth(trim.getWidth());
                dto.setHeight(trim.getHeight());
                dto.setTrunkVolume(trim.getTrunkVolume());
                dto.setWeight(trim.getWeight());
                dto.setFuelType(trim.getFuelType());
                dto.setFuelGrade(trim.getFuelGrade());
                dto.setFuelTankCapacity(trim.getFuelTankCapacity());
                dto.setDoors(trim.getDoors());
                dto.setReversingCamera(trim.getReversingCamera() != null && trim.getReversingCamera() == 1);
                dto.setAutoHold(trim.getAutoHold() != null && trim.getAutoHold() == 1);
                dto.setDriverAssist(trim.getDriverAssist());
                dto.setSmartConnect(trim.getSmartConnect());
                dto.setElectricSeat(trim.getElectricSeat() != null && trim.getElectricSeat() == 1);
                dto.setSeatFunction(trim.getSeatFunction());
                try {
                    dto.setFeatures(parseList(trim.getFeatures()));
                    dto.setImages(parseList(trim.getImages()));
                    dto.setInteriorImages(parseList(trim.getInteriorImages()));
                    dto.setDescriptions(parseList(trim.getDescriptions()));
                } catch (Exception e) {
                    dto.setFeatures(List.of());
                    dto.setImages(List.of());
                    dto.setInteriorImages(List.of());
                    dto.setDescriptions(List.of());
                }
            }

            Store store = storeMapper.selectById(c.getStoreId());
            if (store != null) {
                dto.setStoreName(store.getName());
            }

            result.add(dto);
        }
        return result;
    }

    public void updateCarStatus(Long id, String status) {
        Car car = carMapper.selectById(id);
        if (car != null) {
            car.setStatus(status);
            carMapper.updateById(car);
        }
    }

    public void updateCar(Long id, UpdateCarRequest req) {
        Car car = carMapper.selectById(id);
        if (car == null) {
            throw new BusinessException("车辆不存在");
        }

        if (req.getMileage() != null) {
            car.setMileage(req.getMileage());
            carMapper.updateById(car);
        }

        if (req.getPrice() != null || req.getImage() != null) {
            CarTrim trim = carTrimMapper.selectById(car.getTrimId());
            if (trim != null) {
                if (req.getPrice() != null) {
                    trim.setRentalPrice(java.math.BigDecimal.valueOf(req.getPrice()));
                }
                if (req.getImage() != null) {
                    trim.setImage(req.getImage());
                }
                carTrimMapper.updateById(trim);
            }
        }
    }

    public void deleteCar(Long id) {
        carMapper.deleteById(id);
    }

    public List<StoreDTO> getStores() {
        List<Store> stores = storeMapper.selectList(null);
        List<StoreDTO> result = new ArrayList<>();
        for (Store s : stores) {
            StoreDTO dto = new StoreDTO();
            dto.setId(s.getId());
            dto.setName(s.getName());
            dto.setProvince(s.getProvince());
            dto.setCity(s.getCity());
            dto.setAddress(s.getAddress());
            dto.setPhone(s.getPhone());
            dto.setLat(s.getLat());
            dto.setLng(s.getLng());
            dto.setHours(s.getHours());
            dto.setStatus(s.getStatus() != null && s.getStatus() == 1 ? "active" : "closed");
            long carCount = carMapper.selectCount(
                    new LambdaQueryWrapper<Car>().eq(Car::getStoreId, s.getId()));
            dto.setCarCount(carCount);
            result.add(dto);
        }
        return result;
    }

    public void updateStoreStatus(Long id, String status) {
        Store store = storeMapper.selectById(id);
        if (store != null) {
            store.setStatus("active".equals(status) ? 1 : 0);
            storeMapper.updateById(store);
        }
    }

    public Store createStore(Store store) {
        store.setStatus(1);
        storeMapper.insert(store);
        return store;
    }

    public Store updateStore(Long id, Store store) {
        Store existing = storeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("门店不存在");
        }
        store.setId(id);
        storeMapper.updateById(store);
        return storeMapper.selectById(id);
    }

    public void deleteStore(Long id) {
        long carCount = carMapper.selectCount(
                new LambdaQueryWrapper<Car>().eq(Car::getStoreId, id));
        if (carCount > 0) {
            throw new BusinessException("该门店下仍有车辆，无法删除");
        }
        storeMapper.deleteById(id);
    }

    private List<String> parseList(String json) throws Exception {
        if (json == null || json.isBlank()) return List.of();
        return objectMapper.readValue(json, new TypeReference<List<String>>() {});
    }

    private String getInsuranceName(String code) {
        if (code == null) return "";
        return switch (code) {
            case "basic" -> "基础保障服务";
            case "premium" -> "尊享服务";
            case "premium_plus" -> "尊享百万升级版";
            default -> code;
        };
    }
}
