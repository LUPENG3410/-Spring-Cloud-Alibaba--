CREATE DATABASE IF NOT EXISTS `changxing_review` DEFAULT CHARSET utf8mb4;

USE `changxing_review`;

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    trim_id BIGINT NOT NULL COMMENT '车款ID',
    booking_id BIGINT COMMENT '关联订单ID',
    rating TINYINT NOT NULL COMMENT '评分(1-5)',
    content TEXT COMMENT '评价内容',
    car_days INT COMMENT '租车天数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_trim_id (trim_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户评价表';
