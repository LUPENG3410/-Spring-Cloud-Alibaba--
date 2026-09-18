-- 添加取还车时间字段
ALTER TABLE bookings ADD COLUMN pickup_time DATETIME COMMENT '取车时间';
ALTER TABLE bookings ADD COLUMN return_time DATETIME COMMENT '还车时间';
