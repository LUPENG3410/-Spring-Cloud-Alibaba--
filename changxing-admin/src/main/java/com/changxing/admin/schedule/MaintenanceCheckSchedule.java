package com.changxing.admin.schedule;

import com.changxing.admin.service.MaintenanceReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceCheckSchedule {

    private final MaintenanceReminderService maintenanceReminderService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void dailyMaintenanceCheck() {
        log.info("开始每日车辆保养检查...");
        try {
            maintenanceReminderService.checkAndCreateNotifications();
            log.info("每日车辆保养检查完成");
        } catch (Exception e) {
            log.error("每日车辆保养检查失败", e);
        }
    }
}
