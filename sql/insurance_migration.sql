-- 保险保障服务数据库迁移脚本
-- 创建时间: 2026-06-30
-- 说明: 添加保险产品表和订单保险字段

-- ============================================
-- 1. 创建保险产品表
-- ============================================
CREATE TABLE IF NOT EXISTS insurance_products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '保险产品ID',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '产品编码: basic/premium/premium_plus',
    name VARCHAR(50) NOT NULL COMMENT '产品名称',
    description TEXT COMMENT '产品描述',
    daily_price DECIMAL(10,2) NOT NULL COMMENT '每日保费(元)',
    total_price DECIMAL(10,2) NOT NULL COMMENT '总保费(元)',
    
    -- 车辆损失保障
    vehicle_loss_deductible DECIMAL(10,2) DEFAULT 0 COMMENT '车辆损失免赔额(元)',
    vehicle_loss_coverage_rate DECIMAL(5,2) DEFAULT 100 COMMENT '车辆损失赔付比例(%)',
    
    -- 单独轮胎损失
    tire_loss_covered TINYINT DEFAULT 0 COMMENT '单独轮胎损失是否保障: 0-否 1-是',
    
    -- 第三者责任
    third_party_limit DECIMAL(12,2) DEFAULT 200000 COMMENT '第三者责任险限额(元)',
    
    -- 第三者医保外医疗
    third_party_medical_covered TINYINT DEFAULT 0 COMMENT '第三者医保外医疗是否保障: 0-否 1-是',
    third_party_medical_limit DECIMAL(12,2) DEFAULT 0 COMMENT '第三者医保外医疗限额(元)',
    
    -- 驾驶员损失
    driver_loss_limit DECIMAL(12,2) DEFAULT 50000 COMMENT '驾驶员损失保障限额(元)',
    
    -- 停运费
    stop_fee_covered TINYINT DEFAULT 1 COMMENT '停运费是否保障: 0-否 1-是',
    
    -- 单方事故免材料
    single_accident_no_doc_limit DECIMAL(10,2) DEFAULT 0 COMMENT '单方事故免材料限额(元)',
    
    -- 状态
    status TINYINT DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保险产品表';

-- ============================================
-- 2. 插入三个保险产品
-- ============================================
INSERT INTO insurance_products (code, name, description, daily_price, total_price, 
    vehicle_loss_deductible, vehicle_loss_coverage_rate, tire_loss_covered,
    third_party_limit, third_party_medical_covered, third_party_medical_limit,
    driver_loss_limit, stop_fee_covered, single_accident_no_doc_limit, sort_order) VALUES
('basic', '基础保障服务', '车辆损失1500元免赔额，20万三者险，5万驾驶员保障', 0, 0, 
    1500, 100, 0, 200000, 0, 0, 50000, 1, 0, 1),
('premium', '尊享服务', '车辆损失无免赔，轮胎损失保障，30万三者险', 30, 30, 
    0, 100, 1, 300000, 0, 0, 50000, 1, 5000, 2),
('premium_plus', '尊享百万升级版服务', '车辆损失无免赔，100万三者险，10万医保外医疗，10万驾驶员保障', 50, 50, 
    0, 100, 1, 1000000, 1, 100000, 100000, 1, 5000, 3);

-- ============================================
-- 3. 订单表增加保险相关字段
-- ============================================
ALTER TABLE bookings
ADD COLUMN insurance_product_id BIGINT COMMENT '保险产品ID',
ADD COLUMN insurance_price DECIMAL(10,2) DEFAULT 0 COMMENT '保险费用(元)',
ADD COLUMN insurance_code VARCHAR(20) COMMENT '保险产品编码';

-- ============================================
-- 4. 添加外键约束
-- ============================================
ALTER TABLE bookings
ADD CONSTRAINT fk_bookings_insurance
FOREIGN KEY (insurance_product_id) REFERENCES insurance_products(id);

-- ============================================
-- 5. 添加索引
-- ============================================
CREATE INDEX idx_bookings_insurance_product_id ON bookings(insurance_product_id);
CREATE INDEX idx_bookings_insurance_code ON bookings(insurance_code);

-- ============================================
-- 6. 更新现有订单，设置默认基础保障
-- ============================================
UPDATE bookings 
SET insurance_code = 'basic' 
WHERE insurance_code IS NULL;

-- ============================================
-- 7. 验证迁移结果
-- ============================================
SELECT 'Insurance products created:' AS message;
SELECT * FROM insurance_products;

SELECT 'Bookings updated:' AS message;
SELECT COUNT(*) AS total_bookings, 
       SUM(CASE WHEN insurance_code = 'basic' THEN 1 ELSE 0 END) AS basic_count,
       SUM(CASE WHEN insurance_code = 'premium' THEN 1 ELSE 0 END) AS premium_count,
       SUM(CASE WHEN insurance_code = 'premium_plus' THEN 1 ELSE 0 END) AS premium_plus_count
FROM bookings;
