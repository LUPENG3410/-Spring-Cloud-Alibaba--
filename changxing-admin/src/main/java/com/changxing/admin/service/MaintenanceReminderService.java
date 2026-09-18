package com.changxing.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.changxing.admin.dto.MaintenanceReminderDTO;
import com.changxing.admin.entity.Car;
import com.changxing.admin.entity.CarTrim;
import com.changxing.admin.entity.Store;
import com.changxing.admin.mapper.CarMapper;
import com.changxing.admin.mapper.CarTrimMapper;
import com.changxing.admin.mapper.StoreMapper;
import com.changxing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MaintenanceReminderService {

    private final CarMapper carMapper;
    private final CarTrimMapper carTrimMapper;
    private final StoreMapper storeMapper;
    private final AdminNotificationService notificationService;

    private static final int UPCOMING_DAYS_THRESHOLD = 7;
    private static final Long DEFAULT_ADMIN_ID = 1L;

    public List<MaintenanceReminderDTO> getMaintenanceReminders() {
        List<Car> cars = carMapper.selectList(
                new LambdaQueryWrapper<Car>()
                        .isNotNull(Car::getLastMaintenance)
                        .ne(Car::getStatus, "deleted"));
        return filterReminders(cars, 0);
    }

    public List<MaintenanceReminderDTO> getOverdueReminders() {
        List<Car> cars = carMapper.selectList(
                new LambdaQueryWrapper<Car>()
                        .isNotNull(Car::getLastMaintenance)
                        .ne(Car::getStatus, "deleted"));
        return filterReminders(cars, 0);
    }

    public List<MaintenanceReminderDTO> getUpcomingReminders(int days) {
        List<Car> cars = carMapper.selectList(
                new LambdaQueryWrapper<Car>()
                        .isNotNull(Car::getLastMaintenance)
                        .ne(Car::getStatus, "deleted"));
        return filterReminders(cars, days);
    }

    private List<MaintenanceReminderDTO> filterReminders(List<Car> cars, int upcomingDaysThreshold) {
        List<MaintenanceReminderDTO> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int threshold = upcomingDaysThreshold > 0 ? upcomingDaysThreshold : UPCOMING_DAYS_THRESHOLD;

        System.out.println("=== 保养提醒调试 ===");
        System.out.println("今日日期: " + today);
        System.out.println("查询到车辆数量: " + cars.size());
        int debugCount = 0;

        for (Car car : cars) {
            int intervalDays = car.getMaintenanceIntervalDays() != null ? car.getMaintenanceIntervalDays() : 120;
            LocalDate nextMaintenance = car.getLastMaintenance().plusDays(intervalDays);
            long daysRemaining = ChronoUnit.DAYS.between(today, nextMaintenance);

            if (debugCount < 5) {
                System.out.println("车辆ID: " + car.getId() + ", 车牌: " + car.getPlateNumber()
                    + ", 上次保养: " + car.getLastMaintenance()
                    + ", 保养周期: " + intervalDays + "天"
                    + ", 下次保养: " + nextMaintenance
                    + ", 剩余天数: " + daysRemaining);
                debugCount++;
            }

            String status;
            if (daysRemaining < 0) {
                status = "overdue";
            } else if (daysRemaining <= threshold) {
                status = "upcoming";
            } else {
                continue;
            }

            MaintenanceReminderDTO dto = new MaintenanceReminderDTO();
            dto.setCarId(car.getId());
            dto.setPlateNumber(car.getPlateNumber());
            dto.setLastMaintenance(car.getLastMaintenance().toString());
            dto.setNextMaintenanceDate(nextMaintenance.toString());
            dto.setDaysRemaining((int) daysRemaining);
            dto.setStatus(status);

            CarTrim trim = carTrimMapper.selectById(car.getTrimId());
            if (trim != null) {
                dto.setCarName(trim.getName());
            }

            Store store = storeMapper.selectById(car.getStoreId());
            if (store != null) {
                dto.setStoreId(store.getId());
                dto.setStoreName(store.getName());
                dto.setStorePhone(store.getPhone());
            }

            result.add(dto);
        }
        System.out.println("筛选出需要提醒的车辆数量: " + result.size());
        System.out.println("====================");
        return result;
    }

    public void updateMaintenanceInterval(Long carId, Integer days) {
        if (days == null || days <= 0) {
            throw new BusinessException("保养周期必须大于0天");
        }
        Car car = carMapper.selectById(carId);
        if (car == null) {
            throw new BusinessException("车辆不存在");
        }
        car.setMaintenanceIntervalDays(days);
        carMapper.updateById(car);
    }

    public void markMaintenanceDone(Long carId) {
        Car car = carMapper.selectById(carId);
        if (car == null) {
            throw new BusinessException("车辆不存在");
        }
        car.setLastMaintenance(LocalDate.now());
        carMapper.updateById(car);
    }

    public void checkAndCreateNotifications() {
        List<MaintenanceReminderDTO> reminders = getMaintenanceReminders();
        for (MaintenanceReminderDTO reminder : reminders) {
            if (!notificationService.hasMaintenanceNotification(DEFAULT_ADMIN_ID, reminder.getCarId())) {
                String title;
                String content;
                if ("overdue".equals(reminder.getStatus())) {
                    title = "车辆保养逾期提醒";
                    content = String.format("车牌号 %s 已逾期 %d 天未保养，请尽快通知门店 %s 处理。",
                            reminder.getPlateNumber(),
                            Math.abs(reminder.getDaysRemaining()),
                            reminder.getStoreName());
                } else {
                    title = "车辆保养即将到期提醒";
                    content = String.format("车牌号 %s 将在 %d 天后到期保养，请通知门店 %s 安排保养。",
                            reminder.getPlateNumber(),
                            reminder.getDaysRemaining(),
                            reminder.getStoreName());
                }
                notificationService.createNotification(
                        DEFAULT_ADMIN_ID, title, content, "maintenance", reminder.getCarId());
            }
        }
    }

    public List<Map<String, Object>> getAllCarsMaintenanceStatus() {
        List<Car> cars = carMapper.selectList(
                new LambdaQueryWrapper<Car>()
                        .isNotNull(Car::getLastMaintenance)
                        .ne(Car::getStatus, "deleted"));
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Car car : cars) {
            int intervalDays = car.getMaintenanceIntervalDays() != null ? car.getMaintenanceIntervalDays() : 120;
            LocalDate nextMaintenance = car.getLastMaintenance().plusDays(intervalDays);
            long daysRemaining = ChronoUnit.DAYS.between(today, nextMaintenance);

            String status;
            if (daysRemaining < 0) {
                status = "overdue";
            } else if (daysRemaining <= 7) {
                status = "upcoming";
            } else {
                status = "normal";
            }

            Map<String, Object> item = new HashMap<>();
            item.put("carId", car.getId());
            item.put("plateNumber", car.getPlateNumber());
            item.put("lastMaintenance", car.getLastMaintenance().toString());
            item.put("maintenanceIntervalDays", intervalDays);
            item.put("nextMaintenanceDate", nextMaintenance.toString());
            item.put("daysRemaining", daysRemaining);
            item.put("status", status);

            result.add(item);
        }
        return result;
    }
}
