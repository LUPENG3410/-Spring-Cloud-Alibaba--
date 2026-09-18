package com.changxing.admin.controller;

import com.changxing.admin.dto.MaintenanceReminderDTO;
import com.changxing.admin.service.MaintenanceReminderService;
import com.changxing.common.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/maintenance")
@RequiredArgsConstructor
public class MaintenanceReminderController {

    private final MaintenanceReminderService maintenanceReminderService;

    @GetMapping("/reminders")
    public Result<List<MaintenanceReminderDTO>> getMaintenanceReminders() {
        return Result.success(maintenanceReminderService.getMaintenanceReminders());
    }

    @GetMapping("/overdue")
    public Result<List<MaintenanceReminderDTO>> getOverdueReminders() {
        return Result.success(maintenanceReminderService.getOverdueReminders());
    }

    @GetMapping("/upcoming")
    public Result<List<MaintenanceReminderDTO>> getUpcomingReminders(
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(maintenanceReminderService.getUpcomingReminders(days));
    }

    @GetMapping("/all")
    public Result<List<Map<String, Object>>> getAllCarsMaintenanceStatus() {
        return Result.success(maintenanceReminderService.getAllCarsMaintenanceStatus());
    }

    @PutMapping("/{carId}/interval")
    public Result<Void> updateMaintenanceInterval(
            @PathVariable Long carId,
            @RequestBody Map<String, Integer> body) {
        maintenanceReminderService.updateMaintenanceInterval(carId, body.get("days"));
        return Result.success();
    }

    @PutMapping("/{carId}/complete")
    public Result<Void> markMaintenanceDone(@PathVariable Long carId) {
        maintenanceReminderService.markMaintenanceDone(carId);
        return Result.success();
    }
}
