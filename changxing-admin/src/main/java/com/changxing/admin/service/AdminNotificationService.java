package com.changxing.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.changxing.admin.dto.AdminNotificationDTO;
import com.changxing.admin.entity.AdminNotification;
import com.changxing.admin.mapper.AdminNotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final AdminNotificationMapper notificationMapper;

    public List<AdminNotificationDTO> getNotifications(Long adminId) {
        List<AdminNotification> notifications = notificationMapper.selectList(
                new LambdaQueryWrapper<AdminNotification>()
                        .eq(AdminNotification::getAdminId, adminId)
                        .orderByDesc(AdminNotification::getCreatedAt));
        List<AdminNotificationDTO> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (AdminNotification n : notifications) {
            AdminNotificationDTO dto = new AdminNotificationDTO();
            dto.setId(n.getId());
            dto.setTitle(n.getTitle());
            dto.setContent(n.getContent());
            dto.setType(n.getType());
            dto.setRelatedId(n.getRelatedId());
            dto.setIsRead(n.getIsRead() != null && n.getIsRead() == 1);
            if (n.getCreatedAt() != null) {
                dto.setCreatedAt(n.getCreatedAt().format(fmt));
            }
            result.add(dto);
        }
        return result;
    }

    public long getUnreadCount(Long adminId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<AdminNotification>()
                        .eq(AdminNotification::getAdminId, adminId)
                        .eq(AdminNotification::getIsRead, 0));
    }

    public void markAsRead(Long notificationId) {
        AdminNotification notification = notificationMapper.selectById(notificationId);
        if (notification != null) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
    }

    public void markAllAsRead(Long adminId) {
        List<AdminNotification> notifications = notificationMapper.selectList(
                new LambdaQueryWrapper<AdminNotification>()
                        .eq(AdminNotification::getAdminId, adminId)
                        .eq(AdminNotification::getIsRead, 0));
        for (AdminNotification n : notifications) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    public void createNotification(Long adminId, String title, String content, String type, Long relatedId) {
        AdminNotification notification = new AdminNotification();
        notification.setAdminId(adminId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    public boolean hasMaintenanceNotification(Long adminId, Long carId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<AdminNotification>()
                        .eq(AdminNotification::getAdminId, adminId)
                        .eq(AdminNotification::getType, "maintenance")
                        .eq(AdminNotification::getRelatedId, carId)
                        .ge(AdminNotification::getCreatedAt, LocalDateTime.now().toLocalDate().atStartOfDay())) > 0;
    }
}
