# 畅行租车 - 用户端接口文档

## 基本信息

- **Base URL**: `http://localhost:8080/api`
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

**错误响应**:
```json
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

---

## 1. 认证模块 `/auth`

### 1.1 用户登录

```
POST /auth/login
```

**请求体**:
```json
{
  "phone": "13800138000",
  "password": "123456"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "user": {
      "id": 1,
      "name": "张三",
      "phone": "13800138000",
      "avatar": "https://xxx.com/avatar.jpg",
      "memberLevel": "黄金会员",
      "status": "active"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**错误响应**（账号被禁用）:
```json
{
  "code": 403,
  "message": "账号已被禁用",
  "data": null
}
```

### 1.2 用户注册

```
POST /auth/register
```

**请求体**:
```json
{
  "name": "张三",
  "phone": "13800138000",
  "password": "123456"
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
      "name": "张三",
      "phone": "13800138000",
      "avatar": "",
      "memberLevel": "普通会员",
      "status": "active"
    },
    "token": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

---

## 2. 用户模块 `/user`（需认证）

### 2.1 获取用户信息

```
GET /user/profile
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
    "id": 1,
    "name": "张三",
    "phone": "13800138000",
    "avatar": "https://xxx.com/avatar.jpg",
    "memberLevel": "黄金会员",
    "status": "active"
  }
}
```

---

## 3. 文件上传模块 `/upload`（需认证）

### 3.1 上传图片

```
POST /upload/image
```

**请求头**:
```
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | ✓ | 图片文件(JPG/PNG/WEBP) |

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/upload/image \
  -H "Authorization: Bearer <token>" \
  -F "file=@/path/to/car.jpg"
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "url": "http://localhost:9000/changxing/cars/exterior/car_20260623_143022.jpg",
    "fileName": "car_20260623_143022.jpg",
    "size": 245760,
    "contentType": "image/jpeg"
  }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "文件格式不支持，仅支持 JPG/PNG/WEBP",
  "data": null
}
```

### 3.2 批量上传图片

```
POST /upload/images
```

**请求头**:
```
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| files | File[] | ✓ | 图片文件数组，最多10张 |

**响应**:
```json
{
  "code": 200,
  "data": {
    "urls": [
      "http://localhost:9000/changxing/cars/exterior/car_20260623_143022.jpg",
      "http://localhost:9000/changxing/cars/interior/car_20260623_143023.jpg"
    ]
  }
}
```

### 3.3 删除图片

```
DELETE /upload/image
```

**请求头**:
```
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "url": "http://localhost:9000/changxing/cars/exterior/car_20260623_143022.jpg"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "文件已删除"
}
```

### 上传限制

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 单文件最大 | 5MB | 超过返回错误 |
| 允许格式 | JPG, PNG, WEBP | 其他格式拒绝 |
| 存储桶 | changxing | MinIO bucket |
| 访问路径 | /changxing/cars/{type}/{filename} | type: exterior/interior/avatar |
| URL有效期 | 永久 | 配置为public read |

### MinIO配置

**application.yml**:
```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: changxing
  region: cn-east-1
```

---

## 4. 车辆模块 `/cars`

### 4.1 获取车款列表（基础信息）

```
GET /cars
```

**说明**: 返回车款基础信息，用于列表展示。

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| brand | string | 否 | 品牌筛选 |
| type | string | 否 | 车辆类型: sedan/suv/mpv/luxury/sports |
| province | string | 否 | 省份筛选(有库存的门店) |
| keyword | string | 否 | 搜索关键词(名称/品牌) |

**响应**（Car 基础类型）:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "朗逸 2024款 1.5L 自动舒适版",
      "brand": "大众",
      "type": "sedan",
      "image": "https://images.unsplash.com/...",
      "price": 12.80,
      "rentalPrice": 128,
      "seats": 5,
      "fuel": "92号汽油",
      "transmission": "自动",
      "available": true,
      "features": ["倒车影像", "定速巡航", "自动空调"],
      "province": "北京"
    }
  ]
}
```

### 4.2 获取车款详情（完整信息）

```
GET /cars/:id
```

**说明**: 返回车款完整信息，包含详细参数、图片、描述、评价等。

**响应**（CarDetail 完整类型）:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "朗逸 2024款 1.5L 自动舒适版",
    "brand": "大众",
    "type": "sedan",
    "image": "https://images.unsplash.com/...",
    "price": 12.80,
    "rentalPrice": 128,
    "seats": 5,
    "fuel": "92号汽油",
    "transmission": "自动",
    "available": true,
    "features": ["倒车影像", "定速巡航", "自动空调", "多功能方向盘", "蓝牙连接", "后排出风口"],
    "province": "北京",
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
    "fuelType": "汽油",
    "fuelGrade": "92号",
    "fuelTankCapacity": "55L",
    "doors": "4门",
    "reversingCamera": true,
    "autoHold": false,
    "driverAssist": "无",
    "smartConnect": "无",
    "electricSeat": false,
    "seatFunction": "无",
    "images": [
      "https://images.unsplash.com/...?w=800&h=500&fit=crop",
      "https://images.unsplash.com/...?w=800&h=500&fit=crop"
    ],
    "interiorImages": [
      "https://images.unsplash.com/...?w=800&h=500&fit=crop"
    ],
    "descriptions": [
      "朗逸作为大众旗下经典家轿，以可靠品质和经济油耗著称",
      "宽敞的乘坐空间和510L超大后备箱，满足家庭出行需求"
    ],
    "reviews": [
      {
        "id": 1,
        "userName": "王先生",
        "avatar": "王",
        "rating": 5,
        "date": "2026-05-20",
        "content": "车况很好，动力够用，油耗确实低",
        "carDays": 3
      }
    ]
  }
}
```

---

## 5. 订单模块 `/bookings`（需认证）

### 4.1 获取订单列表

```
GET /bookings
```

**请求头**:
```
Authorization: Bearer <token>
```

**说明**: 获取当前登录用户的所有订单。

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
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
      "returnProvince": "北京"
    }
  ]
}
```

### 4.2 创建订单

```
POST /bookings
```

**请求头**:
```
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "carId": 2,
  "carName": "凯美瑞",
  "carImage": "https://images.unsplash.com/...",
  "startDate": "2026-06-15",
  "endDate": "2026-06-18",
  "totalDays": 3,
  "totalPrice": 594,
  "pickupLocation": "北京首都机场店",
  "returnLocation": "上海浦东店",
  "pickupProvince": "北京",
  "returnProvince": "上海"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 3,
    "carId": 2,
    "carName": "凯美瑞",
    "carImage": "https://images.unsplash.com/...",
    "startDate": "2026-06-15",
    "endDate": "2026-06-18",
    "totalDays": 3,
    "totalPrice": 594,
    "status": "pending",
    "pickupLocation": "北京首都机场店",
    "returnLocation": "上海浦东店",
    "pickupProvince": "北京",
    "returnProvince": "上海"
  }
}
```

### 4.3 取消订单

```
PUT /bookings/:id/cancel
```

**请求头**:
```
Authorization: Bearer <token>
```

**响应**:
```json
{
  "code": 200,
  "message": "订单已取消"
}
```

---

## 6. 门店模块 `/stores`

### 5.1 获取门店列表

```
GET /stores
```

**说明**: 只返回 `status: "active"` 的营业门店。

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| province | string | 否 | 省份筛选 |

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
      "status": "active"
    }
  ]
}
```

---

## 数据模型

### Car（车款基础信息）
| 字段 | 类型 | 必返回 | 说明 |
|------|------|--------|------|
| id | number | ✓ | 车款ID |
| name | string | ✓ | 车款全名 |
| brand | string | ✓ | 品牌 |
| type | string | ✓ | 类型: sedan/suv/mpv/luxury/sports |
| image | string | ✓ | 封面图URL |
| price | number | ✓ | 官方指导价(万元) |
| rentalPrice | number | ✓ | 日租金(元) |
| seats | number | ✓ | 座位数 |
| fuel | string | ✓ | 燃油类型 |
| transmission | string | ✓ | 变速箱 |
| available | boolean | ✓ | 是否有库存 |
| features | string[] | ✓ | 特色功能列表 |
| province | string | ✓ | 有库存的省份 |

### CarDetail（车款完整信息，继承 Car）
| 字段 | 类型 | 必返回 | 说明 |
|------|------|--------|------|
| year | number | ✓ | 年款 |
| engine | string | ✓ | 发动机型号 |
| horsepower | number | ✓ | 最大马力(匹) |
| torque | number | ✓ | 最大扭矩(N·m) |
| displacement | string | ✓ | 排量 |
| acceleration | string | ✓ | 0-100km/h加速时间 |
| topSpeed | string | ✓ | 最高时速 |
| fuelConsumption | string | ✓ | 综合油耗 |
| wheelbase | string | ✓ | 轴距 |
| length | string | ✓ | 车长 |
| width | string | ✓ | 车宽 |
| height | string | ✓ | 车高 |
| trunkVolume | string | ✓ | 后备箱容积 |
| weight | string | ✓ | 整备质量 |
| fuelType | string | ✓ | 能源类型: 汽油/柴油/纯电/插电混动/增程式 |
| fuelGrade | string | - | 燃油标号(汽油车): 92号/95号/98号 |
| fuelTankCapacity | string | - | 油箱容积(L) |
| doors | string | ✓ | 车门数: 2门/3门/4门/5门 |
| reversingCamera | boolean | ✓ | 倒车影像 |
| autoHold | boolean | ✓ | 自动驻车 |
| driverAssist | string | ✓ | 辅助驾驶类型: 无/L1/L2/L2+/L3 |
| smartConnect | string | ✓ | 智能互联: 无/基础互联/智能互联/高级互联 |
| electricSeat | boolean | ✓ | 电动座椅 |
| seatFunction | string | ✓ | 座椅功能: 无/座椅加热/座椅通风/座椅按摩等 |
| images | string[] | ✓ | 外观图片列表 |
| interiorImages | string[] | ✓ | 内饰图片列表 |
| descriptions | string[] | ✓ | 车辆描述列表 |
| reviews | Review[] | ✓ | 用户评价列表 |

### Review（用户评价）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 评价ID |
| userName | string | 用户名 |
| avatar | string | 用户头像(首字) |
| rating | number | 评分(1-5) |
| date | string | 评价日期 |
| content | string | 评价内容 |
| carDays | number | 租车天数 |

### Booking（订单）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 订单ID |
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

### User（用户）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 用户ID |
| name | string | 姓名 |
| phone | string | 手机号 |
| avatar | string | 头像URL |
| memberLevel | string | 会员等级 |
| status | string | 状态: active(正常)/disabled(禁用) |

### StoreLocation（门店）
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
| status | string | 状态: active(营业)/closed(关闭) |

---

## 前端降级策略

前端通过 `src/api/service.ts` 实现自动降级：

1. 每次请求先尝试调用后端 API
2. 如果后端不可用（超时/网络错误），自动切换为本地假数据
3. 首次失败后，后续请求直接使用本地数据，不再尝试后端
4. 配置项 `VITE_API_BASE_URL` 在 `.env` 文件中设置

```
# .env
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 状态规则

### 用户状态
- 新注册用户默认 `status: "active"`
- 管理员可将用户设为 `disabled`，禁用后无法登录
- 登录时检查状态，禁用则返回错误码

### 门店状态
- 只返回 `status: "active"` 的门店给用户端
- `closed` 状态的门店对用户不可见

### 订单状态流转
```
pending → confirmed → completed
    ↓
cancelled
```
- `pending`: 待确认（刚创建）
- `confirmed`: 已确认（管理员确认）
- `completed`: 已完成（还车后）
- `cancelled`: 已取消（用户或管理员取消）

---

## 7. 客服模块 `/service`（需认证）

### 6.1 创建客服会话

```
POST /service/conversations
```

**请求头**:
```
Authorization: Bearer <token>
```

**说明**: 创建一个新的客服会话，返回会话ID供后续消息交互使用。

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": "conv_1719123456789",
    "userId": 1,
    "status": "active",
    "createdAt": "2026-06-23T10:00:00Z",
    "updatedAt": "2026-06-23T10:00:00Z",
    "messages": []
  }
}
```

### 6.2 发送客服消息

```
POST /service/conversations/:conversationId/messages
```

**请求头**:
```
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "content": "我想咨询一下异地还车的费用"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "conversationId": "conv_1719123456789",
    "userId": 1,
    "content": "我想咨询一下异地还车的费用",
    "sender": "user",
    "timestamp": "2026-06-23T10:01:00Z"
  }
}
```

### 6.3 获取会话详情

```
GET /service/conversations/:conversationId
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
    "id": "conv_1719123456789",
    "userId": 1,
    "status": "active",
    "createdAt": "2026-06-23T10:00:00Z",
    "updatedAt": "2026-06-23T10:01:00Z",
    "messages": [
      {
        "id": 1,
        "conversationId": "conv_1719123456789",
        "userId": 1,
        "content": "我想咨询一下异地还车的费用",
        "sender": "user",
        "timestamp": "2026-06-23T10:01:00Z"
      },
      {
        "id": 2,
        "conversationId": "conv_1719123456789",
        "userId": 1,
        "content": "您好！异地还车费用根据取还城市距离计算，通常在100-500元之间。",
        "sender": "agent",
        "timestamp": "2026-06-23T10:01:30Z"
      }
    ]
  }
}
```

### 6.4 获取用户会话列表

```
GET /service/conversations
```

**请求头**:
```
Authorization: Bearer <token>
```

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": "conv_1719123456789",
      "userId": 1,
      "status": "active",
      "createdAt": "2026-06-23T10:00:00Z",
      "updatedAt": "2026-06-23T10:01:00Z",
      "messages": []
    }
  ]
}
```

### 6.5 关闭会话

```
PUT /service/conversations/:conversationId/close
```

**请求头**:
```
Authorization: Bearer <token>
```

**响应**:
```json
{
  "code": 200,
  "message": "会话已关闭"
}
```

### 客服数据模型

#### ServiceConversation（客服会话）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 会话ID |
| userId | number | 用户ID |
| status | string | 状态: active(进行中)/closed(已关闭) |
| createdAt | string | 创建时间(ISO 8601) |
| updatedAt | string | 最后更新时间(ISO 8601) |
| messages | ServiceMessage[] | 消息列表 |

#### ServiceMessage（客服消息）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 消息ID |
| conversationId | string | 所属会话ID |
| userId | number | 用户ID |
| content | string | 消息内容 |
| sender | string | 发送者: user(用户)/agent(客服)/system(系统) |
| timestamp | string | 发送时间(ISO 8601) |

### 客服会话状态流转
```
active → closed
```
- `active`: 进行中（用户可发送消息）
- `closed`: 已关闭（客服或系统关闭）

### 6.6 发送消息并获取Agent回复（SSE流式）

```
POST /service/chat
```

**请求头**:
```
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体**:
```json
{
  "conversationId": "conv_1719123456789",
  "content": "这辆车现在有空吗？",
  "carId": 1,
  "carName": "朗逸"
}
```

**响应**（SSE流式）:
```
data: {"type":"start","messageId":1001}
data: {"type":"delta","content":"您好"}
data: {"type":"delta","content":"！这款车"}
data: {"type":"delta","content":"目前有库存"}
data: {"type":"delta","content":"，可以正常预订。"}
data: {"type":"done","messageId":1001}
```

### 6.7 获取推荐问题

```
GET /service/quick-replies
```

**请求头**:
```
Authorization: Bearer <token>
```

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| carId | number | 否 | 车辆ID，返回该车辆相关问题 |

**响应**:
```json
{
  "code": 200,
  "data": {
    "general": [
      "押金怎么算？",
      "保险包含哪些？",
      "可以开发票吗？",
      "需要什么证件？",
      "可以异地还车吗？"
    ],
    "car": [
      "这辆车现在有空吗？",
      "油耗怎么样？",
      "空间大吗？",
      "适合跑高速吗？"
    ]
  }
}
```

### Agent对接说明

智能客服 AI Agent 采用独立的 **Python (FastAPI)** 服务实现，不部署在 Spring Boot 中。

#### 架构

```
前端 ──► Python Agent (http://localhost:5000/chat/stream) ──► OpenAI API
              │
              └──► Spring Boot Gateway (/api/service/*) 会话/消息 CRUD
```

#### Agent API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/health` | 健康检查 |
| POST | `/chat` | 普通聊天（返回完整回复） |
| POST | `/chat/stream` | SSE 流式聊天（打字机效果） |

**请求体**:
```json
{
  "conversation_id": "conv_123",
  "content": "这辆车有现车吗？",
  "car_id": 1,
  "car_name": "大众朗逸"
}
```

**SSE 流式响应格式**:
```
event: start
data: {"message_id": 1001}

event: delta
data: {"content": "您好"}

event: delta
data: {"content": "！这款车"}

event: done
data: {"message_id": 1001}
```

#### 前端对接

前端通过 `src/api/agent.ts` 对接 Python Agent：
- `agentChat()` - 普通聊天
- `agentChatStream()` - SSE 流式聊天
- 配置项 `VITE_AGENT_URL` 默认 `http://localhost:5000`

#### 项目位置

```
changxing-agent/
├── main.py          # FastAPI 入口
├── agent.py         # Agent 核心（OpenAI 调用）
├── models.py        # Pydantic 模型
├── config.py        # 配置管理
└── requirements.txt # Python 依赖
```
