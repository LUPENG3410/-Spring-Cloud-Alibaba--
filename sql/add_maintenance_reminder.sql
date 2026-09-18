-- 车辆保养提醒功能 - 数据库变更脚本

-- 1. cars 表添加保养周期字段
ALTER TABLE cars ADD COLUMN maintenance_interval_days INT DEFAULT 120 COMMENT '保养周期天数';

-- 如果字段已存在，更新默认值为120
UPDATE cars SET maintenance_interval_days = 120 WHERE maintenance_interval_days = 180;

-- 2. 创建管理员站内消息表
CREATE TABLE IF NOT EXISTS admin_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    type VARCHAR(50) NOT NULL COMMENT '消息类型: maintenance/booking/system',
    related_id BIGINT COMMENT '关联ID(如车辆ID)',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_id (admin_id),
    INDEX idx_is_read (is_read)
) COMMENT '管理员站内消息表';
