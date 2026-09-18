[ADMIN_API.md](../../../my-app/ADMIN_API.md)# 畅行租车 - 数据库设计文档

## 1. 概述

### 1.1 项目信息
- **项目名称**: 畅行租车
- **数据库类型**: MySQL 8.0+（推荐）/ PostgreSQL
- **字符集**: utf8mb4
- **存储引擎**: InnoDB

### 1.2 设计原则
- 第三范式（3NF）为主，适当反范式化提升查询性能
- 软删除（逻辑删除）保护数据安全
- 审计字段记录数据变更

---

## 2. 数据库表设计

### 2.1 用户表 `users`

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    avatar VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    member_level VARCHAR(20) DEFAULT '普通会员' COMMENT '会员等级: 普通会员/白银会员/黄金会员/钻石会员',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间(NULL表示未删除)',
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

**字段说明**:
| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | 是 | 自增 | 主键 |
| name | VARCHAR(50) | 是 | - | 用户姓名 |
| phone | VARCHAR(20) | 是 | - | 手机号，唯一索引 |
| password | VARCHAR(255) | 是 | - | BCrypt加密密码 |
| avatar | VARCHAR(500) | 否 | '' | 头像URL |
| member_level | VARCHAR(20) | 否 | '普通会员' | 会员等级 |
| status | TINYINT | 否 | 1 | 0=禁用, 1=正常 |
| created_at | DATETIME | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | 否 | CURRENT_TIMESTAMP | 更新时间(自动) |
| deleted_at | DATETIME | 否 | NULL | 软删除时间 |

---

### 2.2 管理员表 `admins`

```sql
CREATE TABLE admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(50) NOT NULL UNIQUE COMMENT '账号',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    avatar VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    role VARCHAR(20) DEFAULT 'admin' COMMENT '角色: admin-管理员 superadmin-超级管理员',
    last_login DATETIME COMMENT '最后登录时间',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_phone (phone),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';
```

---

### 2.3 品牌表 `car_brands`

```sql
CREATE TABLE car_brands (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '品牌ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '品牌名称',
    logo VARCHAR(500) COMMENT '品牌Logo',
    country VARCHAR(50) COMMENT '所属国家',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌表';
```

**初始数据**:
```sql
INSERT INTO car_brands (name, country, sort_order) VALUES
('大众', '德国', 1),
('丰田', '日本', 2),
('本田', '日本', 3),
('别克', '美国', 4),
('宝马', '德国', 5),
('奔驰', '德国', 6),
('保时捷', '德国', 7),
('日产', '日本', 8),
('奥迪', '德国', 9),
('哈弗', '中国', 10);
```

---

### 2.4 车系表 `car_series`

```sql
CREATE TABLE car_series (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '车系ID',
    brand_id BIGINT NOT NULL COMMENT '品牌ID',
    name VARCHAR(100) NOT NULL COMMENT '车系名称',
    type VARCHAR(20) NOT NULL COMMENT '类型: sedan/suv/mpv/luxury/sports',
    image VARCHAR(500) COMMENT '车系图片',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_brand_id (brand_id),
    FOREIGN KEY (brand_id) REFERENCES car_brands(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车系表';
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键 |
| brand_id | BIGINT | 是 | 外键关联car_brands |
| name | VARCHAR(100) | 是 | 车系名称(如: 朗逸、凯美瑞) |
| type | VARCHAR(20) | 是 | 车辆类型 |
| image | VARCHAR(500) | 否 | 车系图片 |

**初始数据**:
```sql
INSERT INTO car_series (brand_id, name, type) VALUES
(1, '朗逸', 'sedan'),
(1, '速腾', 'sedan'),
(2, '凯美瑞', 'sedan'),
(2, '汉兰达', 'suv'),
(3, 'CR-V', 'suv'),
(4, 'GL8', 'mpv'),
(5, '3系', 'luxury'),
(6, 'C级', 'luxury'),
(7, '718', 'sports'),
(8, '轩逸', 'sedan'),
(9, 'A4L', 'luxury'),
(10, 'H6', 'suv');
```

---

### 2.5 车款表 `car_trims`

```sql
CREATE TABLE car_trims (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '车款ID',
    series_id BIGINT NOT NULL COMMENT '车系ID',
    name VARCHAR(200) NOT NULL COMMENT '车款名称(如: 朗逸 2024款 1.5L 自动舒适版)',
    year INT NOT NULL COMMENT '年款',
    price DECIMAL(10,2) NOT NULL COMMENT '官方指导价(万元)',
    rental_price DECIMAL(10,2) NOT NULL COMMENT '日租金(元)',
    seats INT NOT NULL COMMENT '座位数',
    fuel VARCHAR(50) NOT NULL COMMENT '燃油类型',
    transmission VARCHAR(50) NOT NULL COMMENT '变速箱',
    
    -- 动力参数
    engine VARCHAR(100) COMMENT '发动机型号',
    horsepower INT COMMENT '最大马力(匹)',
    torque INT COMMENT '最大扭矩(N·m)',
    displacement VARCHAR(20) COMMENT '排量',
    
    -- 性能参数
    acceleration VARCHAR(20) COMMENT '0-100km/h加速时间',
    top_speed VARCHAR(20) COMMENT '最高时速',
    fuel_consumption VARCHAR(20) COMMENT '综合油耗',
    
    -- 车身尺寸
    wheelbase VARCHAR(20) COMMENT '轴距',
    length VARCHAR(20) COMMENT '车长',
    width VARCHAR(20) COMMENT '车宽',
    height VARCHAR(20) COMMENT '车高',
    trunk_volume VARCHAR(20) COMMENT '后备箱容积',
    weight VARCHAR(20) COMMENT '整备质量',
    
    -- 能源配置
    fuel_type VARCHAR(20) NOT NULL DEFAULT '汽油' COMMENT '能源类型: 汽油/柴油/纯电/插电混动/增程式',
    fuel_grade VARCHAR(20) COMMENT '燃油标号(汽油车): 92号/95号/98号',
    fuel_tank_capacity VARCHAR(20) COMMENT '油箱容积(L)',
    doors VARCHAR(10) NOT NULL DEFAULT '4门' COMMENT '车门数: 2门/3门/4门/5门',
    
    -- 智能配置
    reversing_camera TINYINT DEFAULT 0 COMMENT '倒车影像: 0-无 1-有',
    auto_hold TINYINT DEFAULT 0 COMMENT '自动驻车: 0-无 1-有',
    driver_assist VARCHAR(20) DEFAULT '无' COMMENT '辅助驾驶类型: 无/L1/L2/L2+/L3',
    smart_connect VARCHAR(20) DEFAULT '无' COMMENT '智能互联: 无/基础互联/智能互联/高级互联',
    electric_seat TINYINT DEFAULT 0 COMMENT '电动座椅: 0-无 1-有',
    seat_function VARCHAR(50) DEFAULT '无' COMMENT '座椅功能: 无/座椅加热/座椅通风/座椅按摩等',
    
    -- 状态
    status TINYINT DEFAULT 1 COMMENT '状态: 0-停售 1-在售',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_series_id (series_id),
    INDEX idx_year (year),
    INDEX idx_fuel_type (fuel_type),
    FOREIGN KEY (series_id) REFERENCES car_series(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车款表';
```

---

### 2.6 车辆配置表 `car_features`

```sql
CREATE TABLE car_features (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '配置名称',
    category VARCHAR(50) COMMENT '分类: 安全/舒适/科技/外观',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆配置表';
```

**初始数据**:
```sql
INSERT INTO car_features (name, category, sort_order) VALUES
('倒车影像', '安全', 1),
('定速巡航', '科技', 2),
('自动空调', '舒适', 3),
('多功能方向盘', '舒适', 4),
('蓝牙连接', '科技', 5),
('后排出风口', '舒适', 6),
('全景天窗', '外观', 7),
('车道偏离预警', '安全', 8),
('自适应巡航', '科技', 9),
('360全景影像', '安全', 10),
('HUD抬头显示', '科技', 11),
('BOSE音响', '舒适', 12),
('电动尾门', '舒适', 13),
('无钥匙进入', '科技', 14),
('座椅加热', '舒适', 15),
('自动泊车', '科技', 16);
```

---

### 2.7 车款配置关联表 `car_trim_features`

```sql
CREATE TABLE car_trim_features (
    trim_id BIGINT NOT NULL COMMENT '车款ID',
    feature_id BIGINT NOT NULL COMMENT '配置ID',
    PRIMARY KEY (trim_id, feature_id),
    FOREIGN KEY (trim_id) REFERENCES car_trims(id) ON DELETE CASCADE,
    FOREIGN KEY (feature_id) REFERENCES car_features(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车款配置关联表';
```

---

### 2.8 车辆图片表 `car_images`

```sql
CREATE TABLE car_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图片ID',
    trim_id BIGINT NOT NULL COMMENT '车款ID',
    type VARCHAR(20) NOT NULL COMMENT '类型: exterior-外观 interior-内饰',
    url VARCHAR(500) NOT NULL COMMENT '图片URL',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_trim_id (trim_id),
    FOREIGN KEY (trim_id) REFERENCES car_trims(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆图片表';
```

---

### 2.9 车辆描述表 `car_descriptions`

```sql
CREATE TABLE car_descriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '描述ID',
    trim_id BIGINT NOT NULL COMMENT '车款ID',
    content TEXT NOT NULL COMMENT '描述内容',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_trim_id (trim_id),
    FOREIGN KEY (trim_id) REFERENCES car_trims(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆描述表';
```

---

### 2.10 门店表 `stores`

```sql
CREATE TABLE stores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '门店ID',
    name VARCHAR(100) NOT NULL COMMENT '门店名称',
    province VARCHAR(20) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    address VARCHAR(200) NOT NULL COMMENT '详细地址',
    phone VARCHAR(30) NOT NULL COMMENT '联系电话',
    lat DECIMAL(10,6) COMMENT '纬度',
    lng DECIMAL(10,6) COMMENT '经度',
    hours VARCHAR(50) DEFAULT '08:00-21:00' COMMENT '营业时间',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-关闭 1-营业',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_province (province),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店表';
```

---

### 2.11 库存车辆表 `cars`

```sql
CREATE TABLE cars (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '车辆ID',
    trim_id BIGINT NOT NULL COMMENT '车款ID',
    store_id BIGINT NOT NULL COMMENT '所属门店ID',
    plate_number VARCHAR(20) NOT NULL UNIQUE COMMENT '车牌号',
    province VARCHAR(20) NOT NULL COMMENT '所属省份',
    city VARCHAR(50) NOT NULL COMMENT '所属城市',
    mileage INT DEFAULT 0 COMMENT '当前里程(km)',
    last_maintenance DATE COMMENT '最近保养日期',
    status VARCHAR(20) DEFAULT 'available' COMMENT '状态: available-可用 rented-已出租 maintenance-维修中',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    INDEX idx_trim_id (trim_id),
    INDEX idx_store_id (store_id),
    INDEX idx_province (province),
    INDEX idx_status (status),
    FOREIGN KEY (trim_id) REFERENCES car_trims(id),
    FOREIGN KEY (store_id) REFERENCES stores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存车辆表';
```

**说明**: 同一款车（trim_id相同）可以有多辆库存车辆，每辆有独立的车牌号和状态，且必须关联到一个门店。

---

### 2.12 车辆配置表 `car_configs`

```sql
CREATE TABLE car_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    car_id BIGINT NOT NULL COMMENT '车辆ID',
    fuel_type VARCHAR(20) NOT NULL COMMENT '能源类型: 汽油/柴油/纯电/插电混动/增程式',
    fuel_grade VARCHAR(20) COMMENT '燃油标号: 92号/95号/98号/0号柴油(仅燃油车)',
    fuel_tank_capacity VARCHAR(20) COMMENT '油箱容积(L)',
    doors VARCHAR(10) NOT NULL DEFAULT '4门' COMMENT '车门数: 2门/3门/4门/5门',
    reversing_camera TINYINT DEFAULT 0 COMMENT '倒车影像: 0-无 1-有',
    auto_hold TINYINT DEFAULT 0 COMMENT '自动驻车: 0-无 1-有',
    driver_assist VARCHAR(20) DEFAULT '无' COMMENT '辅助驾驶类型: 无/L1/L2/L2+/L3',
    smart_connect VARCHAR(20) DEFAULT '无' COMMENT '智能互联: 无/基础互联/智能互联/高级互联',
    electric_seat TINYINT DEFAULT 0 COMMENT '电动座椅: 0-无 1-有',
    seat_function VARCHAR(50) DEFAULT '无' COMMENT '座椅功能: 无/座椅加热/座椅通风/座椅按摩/加热+通风等',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_car_id (car_id),
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆配置表';
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键 |
| car_id | BIGINT | 是 | 关联车辆ID |
| fuel_type | VARCHAR(20) | 是 | 能源类型 |
| fuel_grade | VARCHAR(20) | 否 | 燃油标号(仅汽油车) |
| fuel_tank_capacity | VARCHAR(20) | 否 | 油箱容积 |
| doors | VARCHAR(10) | 是 | 车门数 |
| reversing_camera | TINYINT | 否 | 倒车影像 |
| auto_hold | TINYINT | 否 | 自动驻车 |
| driver_assist | VARCHAR(20) | 否 | 辅助驾驶类型 |
| smart_connect | VARCHAR(20) | 否 | 智能互联 |
| electric_seat | TINYINT | 否 | 电动座椅 |
| seat_function | VARCHAR(50) | 否 | 座椅功能 |

---

## 2.13 保险产品表 `insurance_products`

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

---

### 2.14 订单表 `bookings`

```sql
CREATE TABLE bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    car_id BIGINT NOT NULL COMMENT '库存车辆ID',
    trim_id BIGINT NOT NULL COMMENT '车款ID',
    
    -- 时间信息
    start_date DATE NOT NULL COMMENT '取车日期',
    end_date DATE NOT NULL COMMENT '还车日期',
    total_days INT NOT NULL COMMENT '租赁天数',
    
    -- 价格信息
    daily_price DECIMAL(10,2) NOT NULL COMMENT '日租金(元)',
    total_price DECIMAL(10,2) NOT NULL COMMENT '总费用(元)',
    deposit DECIMAL(10,2) DEFAULT 0 COMMENT '押金(元)',
    cross_province_fee DECIMAL(10,2) DEFAULT 0 COMMENT '跨省还车费(元)',
    
    -- 门店信息
    pickup_store_id BIGINT NOT NULL COMMENT '取车门店ID',
    return_store_id BIGINT NOT NULL COMMENT '还车门店ID',
    pickup_province VARCHAR(20) NOT NULL COMMENT '取车省份',
    return_province VARCHAR(20) NOT NULL COMMENT '还车省份',
    
    -- 状态
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending-待确认 confirmed-已确认 active-使用中 completed-已完成 cancelled-已取消',
    
    -- 保险信息
    insurance_product_id BIGINT COMMENT '保险产品ID',
    insurance_price DECIMAL(10,2) DEFAULT 0 COMMENT '保险费用(元)',
    insurance_code VARCHAR(20) COMMENT '保险产品编码',
    
    -- 时间戳
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_car_id (car_id),
    INDEX idx_status (status),
    INDEX idx_order_no (order_no),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (car_id) REFERENCES cars(id),
    FOREIGN KEY (trim_id) REFERENCES car_trims(id),
    FOREIGN KEY (pickup_store_id) REFERENCES stores(id),
    FOREIGN KEY (return_store_id) REFERENCES stores(id),
    FOREIGN KEY (insurance_product_id) REFERENCES insurance_products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

---

### 2.14 用户评价表 `reviews`

```sql
CREATE TABLE reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    trim_id BIGINT NOT NULL COMMENT '车款ID',
    booking_id BIGINT COMMENT '关联订单ID',
    rating TINYINT NOT NULL COMMENT '评分(1-5)',
    content TEXT COMMENT '评价内容',
    car_days INT COMMENT '租车天数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_trim_id (trim_id),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (trim_id) REFERENCES car_trims(id),
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户评价表';
```

---

### 2.15 客服会话表 `conversations`

```sql
CREATE TABLE conversations (
    id VARCHAR(32) PRIMARY KEY COMMENT '会话ID(conv_时间戳)',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-进行中 closed-已关闭',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服会话表';
```

---

### 2.16 客服消息表 `conversation_messages`

```sql
CREATE TABLE conversation_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    conversation_id VARCHAR(32) NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '消息内容',
    sender VARCHAR(20) NOT NULL COMMENT '发送者: user-用户 agent-客服 system-系统',
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服消息表';
```

---

## 3. ER关系图

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ car_brands  │────<│ car_series  │────<│  car_trims  │
│     1:N     │     │     1:N     │     │     1:N     │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │
                    ┌──────────────────────────┼──────────────────────────┐
                    │                          │                          │
              ┌─────┴─────┐              ┌─────┴─────┐              ┌─────┴─────┐
              │car_features│              │car_images │              │car_desc.  │
              │   M:N     │              │    1:N    │              │    1:N    │
              └─────┬─────┘              └───────────┘              └───────────┘
                    │
              ┌─────┴─────────┐
              │car_trim_feat. │
              └───────────────┘

┌─────────────┐     ┌─────────────┐
│    stores   │────<│    cars     │
│     1:N     │     │             │
└─────────────┘     └──────┬──────┘
                          │
                          │ 1:1
                          ▼
                   ┌─────────────┐
                   │ car_configs │
                   └─────────────┘

┌─────────────┐     ┌─────────────┐
│   stores    │     │   users     │
└──────┬──────┘     └──────┬──────┘
       │                   │
       │     ┌─────────────┴─────────────┐
       │     │         bookings          │
       └────>│            N:1            │
             └─────────────┬─────────────┘
                           │
             ┌─────────────┴─────────────┐
             │          reviews          │
             └───────────────────────────┘

┌─────────────┐     ┌──────────────────────┐
│    cars     │────<│  bookings   │
│  (库存车辆) │     │     1:N     │
└──────┬──────┘     └─────────────┘
       │
       │ N:1
       │
┌──────┴──────┐
│  car_trims  │
└─────────────┘

┌─────────────────────┐     ┌──────────────────────┐
│ insurance_products  │────<│      bookings        │
│        1:N          │     │                      │
└─────────────────────┘     └──────────────────────┘

┌─────────────┐     ┌──────────────────────┐
│conversations│────<│conversation_messages │
│     1:N     │     │                      │
└──────┬──────┘     └──────────────────────┘
       │
       │ N:1
       │
┌──────┴──────┐
│    users    │
└─────────────┘
```

---

## 4. 统计数据表

### 5.1 每日统计表 `daily_stats`

```sql
CREATE TABLE daily_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    new_users INT DEFAULT 0 COMMENT '新增用户数',
    new_bookings INT DEFAULT 0 COMMENT '新增订单数',
    completed_bookings INT DEFAULT 0 COMMENT '完成订单数',
    cancelled_bookings INT DEFAULT 0 COMMENT '取消订单数',
    revenue DECIMAL(12,2) DEFAULT 0 COMMENT '营收金额(元)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日统计表';
```

---

## 5. 字段类型对照表

### 5.1 与前端TypeScript类型对应

| 数据库表 | TypeScript类型 | 文件位置 |
|----------|---------------|----------|
| users | User | src/types/index.ts |
| admins | AdminUser | src/types/admin.ts |
| car_brands | - | 仅数据库 |
| car_series | - | 仅数据库 |
| car_trims | CarDetail | src/types/index.ts |
| cars | AdminCar | src/types/admin.ts |
| stores | StoreLocation | src/types/index.ts |
| insurance_products | InsuranceProduct | src/types/insurance.ts |
| bookings | Booking | src/types/index.ts |
| bookings(管理端) | AdminBooking | src/types/admin.ts |
| reviews | Review | src/types/index.ts |
| conversations | ServiceConversation | src/types/index.ts |
| conversation_messages | ServiceMessage | src/types/index.ts |
| car_configs | AdminCar | src/types/admin.ts |

### 5.2 枚举值对照

| 字段 | 数据库值 | 前端显示 |
|------|---------|---------|
| User.status | 0 | disabled/禁用 |
| User.status | 1 | active/正常 |
| Store.status | 0 | closed/已关闭 |
| Store.status | 1 | active/营业中 |
| Car.status | available | 可用 |
| Car.status | rented | 已出租 |
| Car.status | maintenance | 维修中 |
| Booking.status | pending | 待确认 |
| Booking.status | confirmed | 已确认 |
| Booking.status | active | 使用中 |
| Booking.status | completed | 已完成 |
| Booking.status | cancelled | 已取消 |
| Car.type | sedan | 轿车 |
| Car.type | suv | SUV |
| Car.type | mpv | MPV |
| Car.type | luxury | 豪华 |
| Car.type | sports | 跑车 |
| CarConfig.fuel_type | 汽油 | 汽油 |
| CarConfig.fuel_type | 柴油 | 柴油 |
| CarConfig.fuel_type | 纯电 | 纯电动 |
| CarConfig.fuel_type | 插电混动 | 插电式混合动力 |
| CarConfig.fuel_type | 增程式 | 增程式电动 |
| CarConfig.fuel_grade | 92号 | 92号汽油 |
| CarConfig.fuel_grade | 95号 | 95号汽油 |
| CarConfig.fuel_grade | 98号 | 98号汽油 |
| CarConfig.doors | 2门 | 两门 |
| CarConfig.doors | 4门 | 四门 |
| CarConfig.doors | 5门 | 五门 |
| CarConfig.driver_assist | 无 | 无辅助驾驶 |
| CarConfig.driver_assist | L1 | L1级辅助驾驶 |
| CarConfig.driver_assist | L2 | L2级辅助驾驶 |
| CarConfig.driver_assist | L2+ | L2+级辅助驾驶 |
| CarConfig.driver_assist | L3 | L3级自动驾驶 |
| CarConfig.smart_connect | 无 | 无智能互联 |
| CarConfig.smart_connect | 基础互联 | 基础互联(CarPlay/CarLife) |
| CarConfig.smart_connect | 智能互联 | 智能互联(语音控制/远程控制) |
| CarConfig.smart_connect | 高级互联 | 高级互联(OTA升级/智能家居联动) |
| CarConfig.seat_function | 无 | 无座椅功能 |
| CarConfig.seat_function | 座椅加热 | 座椅加热 |
| CarConfig.seat_function | 座椅通风 | 座椅通风 |
| CarConfig.seat_function | 座椅按摩 | 座椅按摩 |
| CarConfig.seat_function | 加热+通风 | 加热+通风 |
| CarConfig.seat_function | 加热+按摩 | 加热+按摩 |
| CarConfig.seat_function | 通风+按摩 | 通风+按摩 |
| CarConfig.seat_function | 加热+通风+按摩 | 加热+通风+按摩 |

---

## 6. 索引设计

### 6.1 主要查询场景索引

```sql
-- 用户查询
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_status ON users(status);

-- 车辆查询
CREATE INDEX idx_cars_trim_id ON cars(trim_id);
CREATE INDEX idx_cars_store_id ON cars(store_id);
CREATE INDEX idx_cars_province ON cars(province);
CREATE INDEX idx_cars_status ON cars(status);
CREATE INDEX idx_cars_trim_province ON cars(trim_id, province);
CREATE INDEX idx_cars_store_status ON cars(store_id, status);

-- 车辆配置查询
CREATE INDEX idx_car_configs_car_id ON car_configs(car_id);
CREATE INDEX idx_car_configs_fuel_type ON car_configs(fuel_type);
CREATE INDEX idx_car_configs_driver_assist ON car_configs(driver_assist);

-- 车款查询
CREATE INDEX idx_trims_series_id ON car_trims(series_id);
CREATE INDEX idx_trims_year ON car_trims(year);

-- 订单查询
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_car_id ON bookings(car_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_dates ON bookings(start_date, end_date);
CREATE INDEX idx_bookings_insurance_product_id ON bookings(insurance_product_id);
CREATE INDEX idx_bookings_insurance_code ON bookings(insurance_code);

-- 门店查询
CREATE INDEX idx_stores_province ON stores(province);
CREATE INDEX idx_stores_status ON stores(status);
```

---

## 7. 数据量预估

| 表名 | 预估数据量 | 说明 |
|------|-----------|------|
| users | 10万+ | 用户增长 |
| admins | 10-50 | 管理员固定 |
| car_brands | 50-100 | 汽车品牌 |
| car_series | 500-1000 | 车系 |
| car_trims | 5000-10000 | 车款配置 |
| cars | 1000-5000 | 库存车辆 |
| car_configs | 1000-5000 | 车辆配置(与cars 1:1) |
| stores | 100-500 | 门店 |
| insurance_products | 3 | 保险产品(固定) |
| bookings | 100万+ | 订单(持续增长) |
| reviews | 10万+ | 评价 |
| conversations | 50万+ | 客服会话 |
| conversation_messages | 500万+ | 客服消息 |

---

## 8. 备份策略

```bash
# 每日凌晨全量备份
mysqldump -u root -p changxing_rental > backup_$(date +%Y%m%d).sql

# binlog增量备份
mysqlbinlog --start-datetime="2026-01-01 00:00:00" binlog.000001 >增量备份.sql
```

---

## 9. 扩展建议

### 9.1 未来可增加的表
- `coupons` - 优惠券表
- `payments` - 支付记录表
- `complaints` - 投诉工单表
- `announcements` - 公告表
- `banners` - 轮播图表
- `insurance_clauses` - 保险条款表
- `insurance_claims` - 理赔记录表

### 9.2 性能优化
- 大表分表：bookings按月分表
- 读写分离：主库写，从库读
- 缓存层：Redis缓存热门车辆数据
