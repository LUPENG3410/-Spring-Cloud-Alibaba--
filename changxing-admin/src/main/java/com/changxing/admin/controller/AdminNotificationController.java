package com.changxing.admin.controller;

import com.changxing.admin.dto.AdminNotificationDTO;
import com.changxing.admin.service.AdminNotificationService;
import com.changxing.common.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService notificationService;

    private static final Long DEFAULT_ADMIN_ID = 1L;

    @GetMapping
    public Result<List<AdminNotificationDTO>> getNotifications() {
        return Result.success(notificationService.getNotifications(DEFAULT_ADMIN_ID));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount(DEFAULT_ADMIN_ID);
        return Result.success(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead(DEFAULT_ADMIN_ID);
        return Result.success();
    }
}
