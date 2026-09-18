-- =====================================================
-- 高并发优化：数据库层 索引 + 归档表 + 防重兜底索引
-- 说明：执行前建议先做外键清理（见 fix_bookings_fk.sql）
-- =====================================================

-- 1) 下单可用性检查核心索引（对齐 checkCarAvailability 的 trim_id + status + 日期重叠查询）
ALTER TABLE `0623`.bookings
  ADD INDEX idx_trim_status_dates (trim_id, status, start_date, end_date);

-- 2) 车辆列表库存判断索引
ALTER TABLE `0623`.cars
  ADD INDEX idx_status_province_trim (status, province, trim_id);

-- 3) 数据库防重兜底（同一 trim + province 相同日期段活跃订单的唯一性防线，配合应用层 Redis 预占）
ALTER TABLE `0623`.bookings
  ADD INDEX idx_trim_province_dates (trim_id, pickup_province, start_date, end_date);

-- 4) 冷热数据分离：归档表（复制主表结构，历史订单迁入后主表只保留活跃订单）
CREATE TABLE IF NOT EXISTS `0623`.bookings_archive LIKE `0623`.bookings;