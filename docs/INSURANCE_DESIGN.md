# 保险保障服务数据库设计

## 1. 设计目标

在订单系统中支持三个保险挡位的标识和选择：
- **基础保障服务**（basic）：默认包含，1500元免赔额
- **尊享服务**（premium）：需单独购买，无免赔额，30万三者险
- **尊享百万升级版服务**（premium_plus）：需单独购买，无免赔额，100万三者险

## 2. 数据库表设计

### 2.1 保险产品表 `insurance_products`

```sql
CREATE TABLE insurance_products (
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
```

**初始数据**:
```sql
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
```

### 2.2 订单表 `bookings` 增加字段

```sql
ALTER TABLE bookings
ADD COLUMN insurance_product_id BIGINT COMMENT '保险产品ID',
ADD COLUMN insurance_price DECIMAL(10,2) DEFAULT 0 COMMENT '保险费用(元)',
ADD COLUMN insurance_code VARCHAR(20) COMMENT '保险产品编码';
```

**字段说明**:
| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| insurance_product_id | BIGINT | 否 | NULL | 关联保险产品ID |
| insurance_price | DECIMAL(10,2) | 否 | 0 | 本次订单的保险费用 |
| insurance_code | VARCHAR(20) | 否 | NULL | 保险产品编码(冗余字段，便于查询) |

**添加外键**:
```sql
ALTER TABLE bookings
ADD CONSTRAINT fk_bookings_insurance
FOREIGN KEY (insurance_product_id) REFERENCES insurance_products(id);
```

## 3. 保险产品对比

| 特性 | 基础保障 | 尊享服务 | 尊享百万升级版 |
|------|----------|----------|----------------|
| 车辆损失免赔额 | 1500元 | 0元 | 0元 |
| 单独轮胎损失 | 不保障 | 100%保障 | 100%保障 |
| 第三者责任限额 | 20万 | 30万 | 100万 |
| 第三者医保外医疗 | 不保障 | 不保障 | 10万 |
| 驾驶员损失限额 | 5万 | 5万 | 10万 |
| 停运费保障 | 100% | 100% | 100% |
| 单方事故免材料 | 不支持 | <5000元免材料 | <5000元免材料 |
| 价格 | 0元(默认) | 30元/天 | 50元/天 |

## 4. 不予赔偿说明（通用）

以下情况基础保障、尊享、尊享百万升级版均不予赔偿：

1. **驾驶人情形**：
   - 交通肇事逃逸
   - 饮酒、吸毒、服用管制药品
   - 无证、驾驶证被扣留/暂扣/吊销/注销
   - 故意破坏现场、毁灭证据
   - 驾驶人非承租人本人
   - 从事违法活动
   - 承载有毒有害/违禁物品
   - 营利性运营、竞赛、试验等非自用活动
   - 超出车辆承载能力的驾驶行为

2. **损失原因**：
   - 战争、军事冲突、恐怖活动、暴乱、污染、核辐射
   - 违反安全装载规定
   - 车辆被转让、改装、加装（未经允许）
   - 故意损坏车辆或制造事故

3. **车辆损失**：
   - 车轮单独损失（基础保障不赔，尊享/尊享百万赔付）
   - 无明显碰撞痕迹的车身划痕
   - 非全车盗抢的零部件被盗
   - 未及时通知导致无法确定损失
   - 涉水后二次启动
   - 报警灯亮起未及时停车
   - 添加不符合要求的液体

4. **其他情形**：
   - 未及时报案
   - 无法提供事故责任认定书
   - 无法提供现场照片
   - 无法提供发票等证明材料
   - 保险公司免赔拒赔情形

## 5. 使用场景

### 5.1 用户下单流程
1. 用户选择车辆和租期
2. 系统展示三个保险挡位供选择
3. 用户选择保险挡位
4. 系统计算总费用（租金 + 保险费）
5. 创建订单时记录 insurance_product_id 和 insurance_price

### 5.2 理赔流程
1. 根据订单的 insurance_code 判断适用的保障规则
2. 根据保障规则计算赔付金额
3. 记录理赔结果

## 6. 查询示例

### 6.1 查询订单的保险信息
```sql
SELECT 
    b.id,
    b.order_no,
    ip.code AS insurance_code,
    ip.name AS insurance_name,
    ip.third_party_limit,
    ip.driver_loss_limit,
    b.insurance_price
FROM bookings b
LEFT JOIN insurance_products ip ON b.insurance_product_id = ip.id
WHERE b.id = 123;
```

### 6.2 统计各保险挡位的订单数量
```sql
SELECT 
    ip.code,
    ip.name,
    COUNT(*) AS order_count,
    SUM(b.insurance_price) AS total_insurance_revenue
FROM bookings b
JOIN insurance_products ip ON b.insurance_product_id = ip.id
WHERE b.status IN ('confirmed', 'active', 'completed')
GROUP BY ip.code, ip.name;
```

## 7. 扩展建议

1. **保险价格动态化**：后续可扩展 insurance_products 表，支持按车型、租期动态定价
2. **保险产品版本管理**：可添加 version 字段，支持产品迭代
3. **保险条款管理**：可新建 insurance_clauses 表，存储详细的保险条款和免赔说明
