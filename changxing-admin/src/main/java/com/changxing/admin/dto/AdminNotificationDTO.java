package com.changxing.admin.dto;

import lombok.Data;

@Data
public class AdminNotificationDTO {

    private Long id;
    private String title;
    private String content;
    private String type;
    private Long relatedId;
    private Boolean isRead;
    private String createdAt;
}
