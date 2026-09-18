package com.changxing.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("admin_notifications")
public class AdminNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long adminId;

    private String title;

    private String content;

    private String type;

    private Long relatedId;

    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
