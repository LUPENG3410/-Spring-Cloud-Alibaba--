# 畅行租车 - 管理员后台接口文档

## 基本信息

- **Base URL**: `http://localhost:8080/api/admin`
- **认证方式**: JWT Bearer Token（Header: `Authorization: Bearer <token>`）
- **数据格式**: JSON
- **响应统一格式**:

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

---

## 1. 管理员认证 `/admin`

### 1.1 管理员登录

```
POST /admin/login
```

**请求体**:
```json
{
  "phone": "admin",
  "password": "admin123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "user": {
      "id": 1,
      "name": "系统管理员",
      "phone": "admin",
      "avatar": "",
      "role": "superadmin",
      "lastLogin": "2026-06-16T10:00:00Z",
      "status": "active"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**默认账号**: `admin` / `admin123`

---

## 2. 仪表盘 `/admin/stats`（需认证）

### 2.1 获取统计数据

```
GET /admin/stats
```

**请求头**:
```
Authorization: Bearer <token>
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "totalCars": 12,
    "availableCars": 10,
    "totalBookings": 156,
    "pendingBookings": 8,
    "totalUsers": 2340,
    "totalStores": 16,
    "monthlyRevenue": 286500,
    "todayBookings": 12
  }
}
```

---

## 3. 订单管理 `/admin/bookings`（需认证）

### 3.1 获取订单列表

```
GET /admin/bookings
```

**说明**: 获取所有用户的订单，包含用户信息。

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1001,
      "userId": 1,
      "userName": "张三",
      "userPhone": "13800138000",
      "carId": 2,
      "carName": "凯美瑞",
      "carImage": "https://images.unsplash.com/...",
      "startDate": "2026-06-15",
      "endDate": "2026-06-18",
      "totalDays": 3,
      "totalPrice": 594,
      "status": "confirmed",
      "pickupLocation": "北京首都机场店",
      "returnLocation": "北京首都机场店",
      "pickupProvince": "北京",
      "returnProvince": "北京",
      "createdAt": "2026-06-10 14:30:00"
    }
  ]
}
```

### 3.2 更新订单状态

```
PUT /admin/bookings/:id/status
```

**请求体**:
```json
{
  "status": "confirmed"
}
```

**可选状态**: `pending` / `confirmed` / `completed` / `cancelled`

**响应**:
```json
{
  "code": 200,
  "message": "订单状态已更新"
}
```

---

## 4. 用户管理 `/admin/users`（需认证）

### 4.1 获取用户列表

```
GET /admin/users
```

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "张三",
      "phone": "13800138000",
      "avatar": "",
      "memberLevel": "黄金会员",
      "registerDate": "2025-03-15",
      "totalOrders": 12,
      "totalSpent": 4580,
      "status": "active"
    }
  ]
}
```

### 4.2 更新用户状态

```
PUT /admin/users/:id/status
```

**请求体**:
```json
{
  "status": "active"
}
```

**可选状态**: `active` / `disabled`

**响应**:
```json
{
  "code": 200,
  "message": "用户状态已更新"
}
```

---

## 5. 车辆管理 `/admin/cars`（需认证）

### 5.1 获取车辆列表

```
GET /admin/cars
```

**说明**: 获取所有车辆的完整信息。

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "朗逸",
      "brand": "大众",
      "type": "sedan",
      "image": "https://images.unsplash.com/...",
      "price": 128,
      "seats": 5,
      "fuel": "92号汽油",
      "transmission": "自动",
      "available": true,
      "features": ["倒车影像", "定速巡航"],
      "province": "北京",
      "mileage": 32000,
      "lastMaintenance": "2026-05-20",
      "status": "available",
      "year": 2024,
      "engine": "1.5L EA211",
      "horsepower": 113,
      "torque": 145,
      "displacement": "1.5L",
      "acceleration": "11.2s",
      "topSpeed": "190km/h",
      "fuelConsumption": "5.9L/100km",
      "wheelbase": "2688mm",
      "length": "4678mm",
      "width": "1806mm",
      "height": "1474mm",
      "trunkVolume": "510L",
      "weight": "1280kg",
      "images": ["https://images.unsplash.com/..."],
      "interiorImages": ["https://images.unsplash.com/..."],
      "descriptions": ["朗逸作为大众旗下经典家轿"]
    }
  ]
}
```

### 5.2 添加车辆

```
POST /admin/cars
```

**请求体**:
```json
{
  "name": "朗逸",
  "brand": "大众",
  "type": "sedan",
  "image": "https://images.unsplash.com/...",
  "price": 128,
  "seats": 5,
  "fuel": "92号汽油",
  "transmission": "自动",
  "available": true,
  "features": ["倒车影像", "定速巡航"],
  "province": "北京",
  "mileage": 0,
  "lastMaintenance": "2026-06-16",
  "status": "available",
  "year": 2024,
  "engine": "1.5L EA211",
  "horsepower": 113,
  "torque": 145,
  "displacement": "1.5L",
  "acceleration": "11.2s",
  "topSpeed": "190km/h",
  "fuelConsumption": "5.9L/100km",
  "wheelbase": "2688mm",
  "length": "4678mm",
  "width": "1806mm",
  "height": "1474mm",
  "trunkVolume": "510L",
  "weight": "1280kg",
  "images": ["https://images.unsplash.com/..."],
  "interiorImages": ["https://images.unsplash.com/..."],
  "descriptions": ["朗逸作为大众旗下经典家轿"]
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 13,
    "name": "朗逸",
    "brand": "大众",
    ...
  }
}
```

### 5.3 更新车辆状态

```
PUT /admin/cars/:id/status
```

**请求体**:
```json
{
  "status": "available"
}
```

**可选状态**: `available` / `rented` / `maintenance`

**响应**:
```json
{
  "code": 200,
  "message": "车辆状态已更新"
}
```

### 5.4 删除车辆

```
DELETE /admin/cars/:id
```

**响应**:
```json
{
  "code": 200,
  "message": "车辆已删除"
}
```

---

## 6. 门店管理 `/admin/stores`（需认证）

### 6.1 获取门店列表

```
GET /admin/stores
```

**说明**: 获取所有门店，包含已关闭的门店。

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "北京首都机场店",
      "province": "北京",
      "city": "北京",
      "address": "北京市顺义区首都机场T3航站楼",
      "phone": "010-88881001",
      "lat": 40.0799,
      "lng": 116.6031,
      "hours": "06:00-23:00",
      "carCount": 15,
      "status": "active"
    }
  ]
}
```

### 6.2 更新门店状态

```
PUT /admin/stores/:id/status
```

**请求体**:
```json
{
  "status": "active"
}
```

**可选状态**: `active` / `closed`

**响应**:
```json
{
  "code": 200,
  "message": "门店状态已更新"
}
```

---

## 数据模型

### AdminUser（管理员）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 管理员ID |
| name | string | 姓名 |
| phone | string | 账号 |
| avatar | string | 头像URL |
| role | string | 角色: admin/superadmin |
| lastLogin | string | 最后登录时间(ISO格式) |
| status | string | 状态: active/disabled |

### AdminStats（统计数据）
| 字段 | 类型 | 说明 |
|------|------|------|
| totalCars | number | 总车辆数 |
| availableCars | number | 可用车辆数 |
| totalBookings | number | 总订单数 |
| pendingBookings | number | 待处理订单数 |
| totalUsers | number | 注册用户数 |
| totalStores | number | 门店总数 |
| monthlyRevenue | number | 本月营收(元) |
| todayBookings | number | 今日订单数 |

### AdminBooking（管理端订单）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 订单ID |
| userId | number | 用户ID |
| userName | string | 用户姓名 |
| userPhone | string | 用户手机 |
| carId | number | 车辆ID |
| carName | string | 车辆名称 |
| carImage | string | 车辆图片 |
| startDate | string | 取车日期(YYYY-MM-DD) |
| endDate | string | 还车日期(YYYY-MM-DD) |
| totalDays | number | 租赁天数 |
| totalPrice | number | 总费用(元) |
| status | string | 状态: pending/confirmed/active/completed/cancelled |
| pickupLocation | string | 取车门店 |
| returnLocation | string | 还车门店 |
| pickupProvince | string | 取车省份 |
| returnProvince | string | 还车省份 |
| createdAt | string | 下单时间(YYYY-MM-DD HH:mm:ss) |

### AdminUserInfo（管理端用户）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 用户ID |
| name | string | 姓名 |
| phone | string | 手机号 |
| avatar | string | 头像URL |
| memberLevel | string | 会员等级 |
| registerDate | string | 注册日期(YYYY-MM-DD) |
| totalOrders | number | 累计订单数 |
| totalSpent | number | 累计消费(元) |
| status | string | 状态: active/disabled |

### AdminCar（管理端车辆）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 车辆ID |
| name | string | 车辆名称 |
| brand | string | 品牌 |
| type | string | 类型: sedan/suv/mpv/luxury/sports |
| image | string | 封面图URL |
| price | number | 日租金(元) |
| seats | number | 座位数 |
| fuel | string | 燃油类型 |
| transmission | string | 变速箱 |
| available | boolean | 是否可用 |
| features | string[] | 特色功能 |
| province | string | 所属省份 |
| storeId | number | 所属门店ID |
| storeName | string | 所属门店名称 |
| mileage | number | 里程数(km) |
| lastMaintenance | string | 最近保养日期(YYYY-MM-DD) |
| status | string | 状态: available/rented/maintenance |
| year | number | 年款 |
| engine | string | 发动机型号 |
| horsepower | number | 最大马力(匹) |
| torque | number | 最大扭矩(N·m) |
| displacement | string | 排量 |
| acceleration | string | 0-100km/h加速时间 |
| topSpeed | string | 最高时速 |
| fuelConsumption | string | 综合油耗 |
| wheelbase | string | 轴距 |
| length | string | 车长 |
| width | string | 车宽 |
| height | string | 车高 |
| trunkVolume | string | 后备箱容积 |
| weight | string | 整备质量 |
| images | string[] | 外观图片列表 |
| interiorImages | string[] | 内饰图片列表 |
| descriptions | string[] | 车辆描述列表 |
| fuelType | string | 能源类型: 汽油/柴油/纯电/插电混动/增程式 |
| fuelGrade | string | 燃油标号: 92号/95号/98号 |
| fuelTankCapacity | string | 油箱容积(L) |
| doors | string | 车门数: 2门/3门/4门/5门 |
| reversingCamera | boolean | 倒车影像 |
| autoHold | boolean | 自动驻车 |
| driverAssist | string | 辅助驾驶类型: 无/L1/L2/L2+/L3 |
| smartConnect | string | 智能互联: 无/基础互联/智能互联/高级互联 |
| electricSeat | boolean | 电动座椅 |
| seatFunction | string | 座椅功能: 无/座椅加热/座椅通风/座椅按摩等 |

### AdminStoreLocation（管理端门店）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 门店ID |
| name | string | 门店名称 |
| province | string | 省份 |
| city | string | 城市 |
| address | string | 详细地址 |
| phone | string | 联系电话 |
| lat | number | 纬度 |
| lng | number | 经度 |
| hours | string | 营业时间 |
| carCount | number | 车辆数量 |
| status | string | 状态: active/closed |

---

## 前端访问方式

| 入口 | URL |
|------|-----|
| 用户端首页 | `http://localhost:5173/` |
| 用户登录 | `http://localhost:5173/login` |
| 管理员登录 | `http://localhost:5173/admin/login` |
| 管理员后台 | `http://localhost:5173/admin` |

---

## 前端降级策略

管理员后台使用 `src/api/admin-service.ts` 实现自动降级：

1. 每次请求先尝试调用后端 API
2. 如果后端不可用，自动切换为本地假数据
3. 本地数据支持完整的 CRUD 操作（添加/修改/删除）
4. 配置项 `VITE_API_BASE_URL` 在 `.env` 文件中设置

---

## 车辆导入功能

管理员添加车辆时，支持从预置车辆数据库导入完整配置：

### 导入流程（4步向导）

1. **选择品牌** - 从预置品牌列表中选择
2. **选择车型** - 选择品牌下的具体车型
3. **选择配置** - 选择具体配置版本，自动填充所有参数
4. **确认信息** - 预览配置详情，设置所属省份、日租金、里程等

### 预置品牌车型

| 品牌 | 车型 |
|------|------|
| 大众 | 朗逸（2款配置）、速腾 |
| 丰田 | 凯美瑞、汉兰达 |
| 本田 | CR-V |
| 别克 | GL8 |
| 宝马 | 3系 |
| 奔驰 | C级 |
| 保时捷 | 718 |
| 日产 | 轩逸 |
| 奥迪 | A4L |
| 哈弗 | H6 |

### 数据库文件

`src/data/carDatabase.ts` - 按品牌→车型→配置组织

### 扩展方式

在 `src/data/carDatabase.ts` 中按照 `CarTemplate` 接口格式添加新车型数据。

---

## 与用户端接口对比

| 模块 | 用户端 | 管理端 |
|------|--------|--------|
| 认证 | `POST /auth/login`, `POST /auth/register` | `POST /admin/login` |
| 用户 | `GET /user/profile` | `GET /admin/users`, `PUT /admin/users/:id/status` |
| 车辆 | `GET /cars`, `GET /cars/:id` | `GET /admin/cars`, `POST /admin/cars`, `PUT /admin/cars/:id/status`, `DELETE /admin/cars/:id` |
| 订单 | `GET /bookings`, `POST /bookings`, `PUT /bookings/:id/cancel` | `GET /admin/bookings`, `PUT /admin/bookings/:id/status` |
| 门店 | `GET /stores`（仅active） | `GET /admin/stores`（全部）, `PUT /admin/stores/:id/status` |
| 统计 | - | `GET /admin/stats` |

---

## 接口总数统计

| 类型 | 用户端 | 管理端 | 合计 |
|------|--------|--------|------|
| GET | 4 | 5 | 9 |
| POST | 2 | 2 | 4 |
| PUT | 1 | 4 | 5 |
| DELETE | 0 | 1 | 1 |
| **合计** | **7** | **12** | **19** |
