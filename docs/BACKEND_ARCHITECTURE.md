# 畅行租车 - 后端架构设计文档

## 1. 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17+ | 运行环境 |
| Spring Boot | 3.2.x | 基础框架 |
| Spring Cloud | 2023.0.x | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.x | 阿里巴巴组件 |
| Spring Cloud Gateway | 4.1.x | API网关 |
| Spring Cloud OpenFeign | 4.1.x | 服务间调用 |
| Nacos | 2.3.x | 服务注册/配置中心 |
| MySQL | 8.0+ | 数据库 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| Redis | 7.x | 缓存/会话 |
| RabbitMQ | 3.12+ | 消息队列 |
| Sentinel | 1.8.x | 流量控制/熔断降级 |
| Seata | 2.0.x | 分布式事务 |
| MinIO | 最新 | 对象存储(图片) |

---

## 2. 整体架构

```
                            ┌─────────────────────────────────────────────────────────────┐
                            │                      客户端 (Client)                        │
                            │   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
                            │   │  用户端   │  │ 管理端   │  │  H5端    │  │  App端   │   │
                            │   └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
                            └────────┼─────────────┼─────────────┼─────────────┼──────────┘
                                     │             │             │             │
                                     └─────────────┴──────┬──────┴─────────────┘
                                                          │
                                                          ▼
                            ┌─────────────────────────────────────────────────────────────┐
                            │                    API Gateway (8080)                       │
                            │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
                            │  │  路由转发    │  │  鉴权过滤    │  │  限流熔断    │         │
                            │  └─────────────┘  └─────────────┘  └─────────────┘         │
                            └───────────────────────────┬─────────────────────────────────┘
                                                        │
                                                        ▼
┌───────────────────────────────────────────────────────────────────────────────────────────┐
│                           Spring Cloud 微服务集群                                         │
│                                                                                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │  用户服务    │  │  车辆服务    │  │  订单服务    │  │  门店服务    │  │  评价服务    │    │
│  │  (9001)     │  │  (9002)     │  │  (9003)     │  │  (9004)     │  │  (9005)     │    │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘    │
│         │                │                │                │                │             │
│         └────────────────┴────────────────┴────────────────┴────────────────┘             │
│                                         │                                                 │
│                                         ▼                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────────────────┐  │
│  │                           基础服务层                                                │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │  │
│  │  │  认证服务    │  │  网关服务    │  │  配置中心    │  │  注册中心    │               │  │
│  │  │  (9000)     │  │  (8080)     │  │  (8848)     │  │  (8848)     │               │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘               │  │
│  └─────────────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                           │
└───────────────────────────────────────────────────────────────────────────────────────────┘
                                                        │
                                                        ▼
                            ┌─────────────────────────────────────────────────────────────┐
                            │                        中间件层                             │
                            │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐       │
                            │  │  MySQL  │  │  Redis  │  │ RabbitMQ│  │  MinIO  │       │
                            │  │ (主从)  │  │ (集群)  │  │ (集群)  │  │ (单机)  │       │
                            │  └─────────┘  └─────────┘  └─────────┘  └─────────┘       │
                            └─────────────────────────────────────────────────────────────┘
```

---

## 3. 服务拆分

### 3.1 服务列表

| 服务名 | 端口 | 说明 | 负责模块 |
|--------|------|------|----------|
| changxing-gateway | 8080 | API网关 | 路由、鉴权、限流 |
| changxing-auth | 9000 | 认证服务 | 登录、注册、JWT |
| changxing-user | 9001 | 用户服务 | 用户信息、会员 |
| changxing-car | 9002 | 车辆服务 | 车辆、品牌、车系 |
| changxing-booking | 9003 | 订单服务 | 订单、支付、分布式锁 |
| changxing-store | 9004 | 门店服务 | 门店、地图 |
| changxing-review | 9005 | 评价服务 | 评价、评分 |
| changxing-admin | 9006 | 管理服务 | 后台管理、统计 |
| changxing-service | 9008 | 客服服务 | 在线客服、会话管理、Agent对接 |
| changxing-upload | 9009 | 上传服务 | 文件上传(MinIO) |
| changxing-common | - | 公共模块 | DTO、工具类、异常 |

### 3.2 服务依赖关系

```
┌──────────────┐
│   Gateway    │
└──────┬───────┘
       │
       ├──────────────────────────────────────────────┐
       │                                              │
       ▼                                              ▼
┌──────────────┐                              ┌──────────────┐
│  Auth Service│◄─────────────────────────────│ User Service │
└──────────────┘                              └──────────────┘
       │                                              │
       ▼                                              ▼
┌──────────────┐                              ┌──────────────┐
│    Redis     │                              │    MySQL     │
└──────────────┘                              └──────────────┘

┌──────────────┐    OpenFeign    ┌──────────────┐
│Booking Service│◄──────────────►│  Car Service  │
└──────┬───────┘                └──────────────┘
       │
       │ OpenFeign
       ▼
┌──────────────┐
│ Store Service│
└──────────────┘
```

---

## 4. 项目结构

```
changxing-rental/
├── changxing-gateway/           # 网关服务
│   ├── src/main/java/
│   │   └── com.changxing.gateway/
│   │       ├── GatewayApplication.java
│   │       ├── config/
│   │       │   ├── CorsConfig.java
│   │       │   └── RouteConfig.java
│   │       └── filter/
│   │           └── AuthFilter.java
│   └── src/main/resources/
│       └── application.yml
│
├── changxing-auth/              # 认证服务
│   ├── src/main/java/
│   │   └── com.changxing.auth/
│   │       ├── AuthApplication.java
│   │       ├── controller/
│   │       │   └── AuthController.java
│   │       ├── service/
│   │       │   └── AuthService.java
│   │       └── dto/
│   │           ├── LoginRequest.java
│   │           └── LoginResponse.java
│   └── src/main/resources/
│       └── application.yml
│
├── changxing-user/              # 用户服务
│   ├── src/main/java/
│   │   └── com.changxing.user/
│   │       ├── UserApplication.java
│   │       ├── controller/
│   │       │   └── UserController.java
│   │       ├── service/
│   │       │   └── UserService.java
│   │       ├── mapper/
│   │       │   └── UserMapper.java
│   │       └── entity/
│   │           └── User.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
│
├── changxing-car/               # 车辆服务
│   ├── src/main/java/
│   │   └── com.changxing.car/
│   │       ├── CarApplication.java
│   │       ├── controller/
│   │       │   ├── CarController.java
│   │       │   ├── BrandController.java
│   │       │   └── SeriesController.java
│   │       ├── service/
│   │       │   └── CarService.java
│   │       ├── mapper/
│   │       │   ├── CarMapper.java
│   │       │   ├── BrandMapper.java
│   │       │   └── SeriesMapper.java
│   │       ├── entity/
│   │       │   ├── Car.java
│   │       │   ├── CarBrand.java
│   │       │   ├── CarSeries.java
│   │       │   └── CarTrim.java
│   │       └── feign/
│   │           └── StoreFeignClient.java
│   └── src/main/resources/
│       └── application.yml
│
├── changxing-booking/           # 订单服务
│   ├── src/main/java/
│   │   └── com.changxing.booking/
│   │       ├── BookingApplication.java
│   │       ├── controller/
│   │       │   └── BookingController.java
│   │       ├── service/
│   │       │   └── BookingService.java
│   │       ├── mapper/
│   │       │   └── BookingMapper.java
│   │       ├── entity/
│   │       │   └── Booking.java
│   │       └── feign/
│   │           ├── CarFeignClient.java
│   │           ├── UserFeignClient.java
│   │           └── StoreFeignClient.java
│   └── src/main/resources/
│       └── application.yml
│
├── changxing-store/             # 门店服务
│   ├── src/main/java/
│   │   └── com.changxing.store/
│   │       ├── StoreApplication.java
│   │       ├── controller/
│   │       │   └── StoreController.java
│   │       ├── service/
│   │       │   └── StoreService.java
│   │       ├── mapper/
│   │       │   └── StoreMapper.java
│   │       └── entity/
│   │           └── Store.java
│   └── src/main/resources/
│       └── application.yml
│
├── changxing-review/            # 评价服务
│   ├── src/main/java/
│   │   └── com.changxing.review/
│   │       ├── ReviewApplication.java
│   │       ├── controller/
│   │       │   └── ReviewController.java
│   │       ├── service/
│   │       │   └── ReviewService.java
│   │       ├── mapper/
│   │       │   └── ReviewMapper.java
│   │       ├── entity/
│   │       │   └── Review.java
│   │       └── feign/
│   │           └── CarFeignClient.java
│   └── src/main/resources/
│       └── application.yml
│
├── changxing-admin/             # 管理服务
│   ├── src/main/java/
│   │   └── com.changxing.admin/
│   │       ├── AdminApplication.java
│   │       ├── controller/
│   │       │   ├── AdminAuthController.java
│   │       │   ├── AdminCarController.java
│   │       │   ├── AdminBookingController.java
│   │       │   ├── AdminUserController.java
│   │       │   └── AdminStoreController.java
│   │       ├── service/
│   │       │   └── AdminService.java
│   │       └── feign/
│   │           ├── CarFeignClient.java
│   │           ├── BookingFeignClient.java
│   │           ├── UserFeignClient.java
│   │           └── StoreFeignClient.java
│   └── src/main/resources/
│       └── application.yml
│
├── changxing-common/            # 公共模块
│   ├── src/main/java/
│   │   └── com.changxing.common/
│   │       ├── dto/
│   │       │   ├── Result.java
│   │       │   └── PageResult.java
│   │       ├── entity/
│   │       │   └── BaseEntity.java
│   │       ├── exception/
│   │       │   ├── BusinessException.java
│   │       │   └── GlobalExceptionHandler.java
│   │       ├── utils/
│   │       │   ├── JwtUtils.java
│   │       │   └── RedisUtils.java
│   │       └── constant/
│   │           └── Constants.java
│   └── pom.xml
│
└── pom.xml                      # 父工程
```

---

## 5. Gateway 网关配置

### 5.1 application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: changxing-gateway
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      
      routes:
        # 认证服务
        - id: changxing-auth
          uri: lb://changxing-auth
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=1
        
        # 用户服务
        - id: changxing-user
          uri: lb://changxing-user
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=1
        
        # 车辆服务
        - id: changxing-car
          uri: lb://changxing-car
          predicates:
            - Path=/api/cars/**, /api/brands/**, /api/series/**
          filters:
            - StripPrefix=1
        
        # 订单服务
        - id: changxing-booking
          uri: lb://changxing-booking
          predicates:
            - Path=/api/bookings/**
          filters:
            - StripPrefix=1
        
        # 门店服务
        - id: changxing-store
          uri: lb://changxing-store
          predicates:
            - Path=/api/stores/**
          filters:
            - StripPrefix=1
        
        # 评价服务
        - id: changxing-review
          uri: lb://changxing-review
          predicates:
            - Path=/api/reviews/**
          filters:
            - StripPrefix=1

        # 文件上传服务
        - id: changxing-upload
          uri: lb://changxing-upload
          predicates:
            - Path=/api/upload/**
          filters:
            - StripPrefix=1

        # 客服服务
        - id: changxing-service
          uri: lb://changxing-service
          predicates:
            - Path=/api/service/**
          filters:
            - StripPrefix=1
        
        # 管理服务
        - id: changxing-admin
          uri: lb://changxing-admin
          predicates:
            - Path=/api/admin/**
          filters:
            - StripPrefix=1
        
      # 默认过滤器
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin

      # 跨域配置
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
```

### 5.2 鉴权过滤器 AuthFilter.java

```java
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    // 白名单路径(不需要鉴权)
    private static final List<String> WHITE_LIST = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/cars",
        "/api/cars/**",
        "/api/brands",
        "/api/series",
        "/api/stores",
        "/api/stores/**",
        "/api/reviews",
        "/api/reviews/**"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // 白名单放行
        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }
        
        // 获取Token
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return unauthorized(exchange, "未提供认证令牌");
        }
        
        // 验证Token
        token = token.substring(7);
        Claims claims = jwtUtils.parseToken(token);
        if (claims == null) {
            return unauthorized(exchange, "令牌无效或已过期");
        }
        
        // 将用户信息传递给下游服务
        ServerHttpRequest request = exchange.getRequest().mutate()
            .header("X-User-Id", claims.getSubject())
            .header("X-User-Role", claims.get("role", String.class))
            .build();
        
        return chain.filter(exchange.mutate().request(request).build());
    }
    
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -100;
    }
}
```

---

## 6. 负载均衡

### 6.1 负载均衡策略

本项目使用 **Spring Cloud LoadBalancer**（替代Ribbon）实现客户端负载均衡。

```
┌─────────────────────────────────────────────────────────────────┐
│                      负载均衡架构                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   客户端请求                                                     │
│       │                                                         │
│       ▼                                                         │
│   ┌─────────────┐                                               │
│   │   Gateway   │  服务发现 (Nacos)                              │
│   └──────┬──────┘                                               │
│          │                                                      │
│          │  lb://changxing-car                                  │
│          ▼                                                      │
│   ┌─────────────┐                                               │
│   │  LoadBalance │  轮询/随机/权重                                │
│   └──────┬──────┘                                               │
│          │                                                      │
│          ├──────────────┬──────────────┐                        │
│          ▼              ▼              ▼                        │
│   ┌─────────────┐ ┌─────────────┐ ┌─────────────┐              │
│   │ Car-9002-1  │ │ Car-9002-2  │ │ Car-9002-3  │              │
│   │  (主节点)    │ │  (从节点)   │ │  (从节点)    │              │
│   └─────────────┘ └─────────────┘ └─────────────┘              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 6.2 Gateway 负载均衡配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: changxing-car
          uri: lb://changxing-car  # lb:// 表示启用负载均衡
          predicates:
            - Path=/api/cars/**
          filters:
            - StripPrefix=1
            # 可选：指定负载均衡策略
            - name: LoadBalancer
              args:
                name: changxing-car
                secure: false
```

### 6.3 Spring Cloud LoadBalancer 配置

```java
@Configuration
public class LoadBalancerConfig {
    
    /**
     * 轮询策略（默认）
     * 请求依次分配到各个实例
     */
    @Bean
    public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RandomLoadBalancer(
            loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
            name);
    }
}
```

### 6.4 可选负载均衡策略

| 策略 | 类名 | 说明 | 适用场景 |
|------|------|------|----------|
| 轮询 | RoundRobinLoadBalancer | 依次分配（默认） | 实例性能相同 |
| 随机 | RandomLoadBalancer | 随机选择 | 实例性能相同 |
| 自定义 | 自定义实现 | 按权重/响应时间 | 实例性能不同 |

### 6.5 OpenFeign 负载均衡

OpenFeign 集成 LoadBalancer，自动实现负载均衡：

```java
// 自动通过服务名找到所有实例，负载均衡选择一个
@FeignClient(name = "changxing-car")
public interface CarFeignClient {
    @GetMapping("/cars/{id}")
    Result<CarDTO> getCar(@PathVariable("id") Long id);
}
```

### 6.6 服务实例健康检查

```yaml
spring:
  cloud:
    loadbalancer:
      health-check:
        interval: 30s  # 健康检查间隔
        path: /actuator/health
      cache:
        enabled: true
        ttl: 30s  # 实例缓存时间
```

### 6.7 多实例部署示例

```yaml
# docker-compose.yml 多实例部署
services:
  car-1:
    build: ./changxing-car
    environment:
      - SERVER_PORT=9002
      - SPRING_APPLICATION_NAME=changxing-car
    depends_on:
      - nacos

  car-2:
    build: ./changxing-car
    environment:
      - SERVER_PORT=9003
      - SPRING_APPLICATION_NAME=changxing-car
    depends_on:
      - nacos

  car-3:
    build: ./changxing-car
    environment:
      - SERVER_PORT=9004
      - SPRING_APPLICATION_NAME=changxing-car
    depends_on:
      - nacos
```

### 6.8 负载均衡流程

```
┌──────────────────────────────────────────────────────────────┐
│                   负载均衡请求流程                             │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  1. 客户端发送请求                                            │
│     GET /api/cars/1                                          │
│                                                              │
│  2. Gateway 路由匹配                                         │
│     Path=/api/cars/** → uri=lb://changxing-car               │
│                                                              │
│  3. 服务发现                                                 │
│     从 Nacos 获取 changxing-car 所有实例:                     │
│     - 192.168.1.101:9002                                     │
│     - 192.168.1.102:9002                                     │
│     - 192.168.1.103:9002                                     │
│                                                              │
│  4. 负载均衡选择                                             │
│     轮询策略: 选择 192.168.1.102:9002                         │
│                                                              │
│  5. 转发请求                                                 │
│     GET http://192.168.1.102:9002/cars/1                     │
│                                                              │
│  6. 返回响应                                                 │
│     Gateway 将响应返回给客户端                                 │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 7. OpenFeign 接口定义

### 6.1 车辆服务 Feign Client

```java
@FeignClient(name = "changxing-car", path = "/api")
public interface CarFeignClient {
    
    @GetMapping("/cars/{id}")
    Result<CarDetailDTO> getCarDetail(@PathVariable("id") Long id);
    
    @GetMapping("/cars/{id}/availability")
    Result<Boolean> checkAvailability(@PathVariable("id") Long id,
                                      @RequestParam("startDate") String startDate,
                                      @RequestParam("endDate") String endDate);
    
    @PutMapping("/cars/{id}/status")
    Result<Void> updateCarStatus(@PathVariable("id") Long id,
                                 @RequestParam("status") String status);
    
    @GetMapping("/trims/{trimId}")
    Result<TrimDTO> getTrimById(@PathVariable("trimId") Long trimId);
}
```

### 6.2 用户服务 Feign Client

```java
@FeignClient(name = "changxing-user", path = "/api")
public interface UserFeignClient {
    
    @GetMapping("/user/profile")
    Result<UserDTO> getUserProfile(@RequestHeader("X-User-Id") Long userId);
    
    @GetMapping("/user/{id}")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);
    
    @PutMapping("/user/{id}/level")
    Result<Void> updateMemberLevel(@PathVariable("id") Long id,
                                   @RequestParam("level") String level);
}
```

### 6.3 门店服务 Feign Client

```java
@FeignClient(name = "changxing-store", path = "/api")
public interface StoreFeignClient {
    
    @GetMapping("/stores/{id}")
    Result<StoreDTO> getStoreById(@PathVariable("id") Long id);
    
    @GetMapping("/stores")
    Result<List<StoreDTO>> getStoresByProvince(@RequestParam(value = "province", required = false) String province);
    
    @GetMapping("/stores/{id}/cars")
    Result<List<CarDTO>> getStoreCars(@PathVariable("id") Long storeId);
}
```

### 6.4 订单服务 Feign Client

```java
@FeignClient(name = "changxing-booking", path = "/api")
public interface BookingFeignClient {
    
    @GetMapping("/bookings/user/{userId}")
    Result<List<BookingDTO>> getUserBookings(@PathVariable("userId") Long userId);
    
    @GetMapping("/bookings/{id}")
    Result<BookingDTO> getBookingById(@PathVariable("id") Long id);
    
    @PutMapping("/bookings/{id}/status")
    Result<Void> updateBookingStatus(@PathVariable("id") Long id,
                                     @RequestParam("status") String status);
}
```

### 6.5 评价服务 Feign Client

```java
@FeignClient(name = "changxing-review", path = "/api")
public interface ReviewFeignClient {
    
    @GetMapping("/reviews/trim/{trimId}")
    Result<List<ReviewDTO>> getTrimReviews(@PathVariable("trimId") Long trimId);
    
    @GetMapping("/reviews/trim/{trimId}/stats")
    Result<ReviewStatsDTO> getTrimReviewStats(@PathVariable("trimId") Long trimId);
}
```

---

## 7. 各服务配置

### 7.1 用户服务 application.yml

```yaml
server:
  port: 9001

spring:
  application:
    name: changxing-user
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yml
  datasource:
    url: jdbc:mysql://localhost:3306/changxing_user?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost
    port: 6379
    database: 0

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deletedAt
      logic-delete-value: "1"
      logic-not-delete-value: "0"

# Feign配置
feign:
  client:
    config:
      default:
        connect-timeout: 5000
        read-timeout: 10000
  sentinel:
    enabled: true
```

### 7.2 车辆服务 application.yml

```yaml
server:
  port: 9002

spring:
  application:
    name: changxing-car
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
  datasource:
    url: jdbc:mysql://localhost:3306/changxing_car?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD:root}
```

### 7.3 订单服务 application.yml

```yaml
server:
  port: 9003

spring:
  application:
    name: changxing-booking
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
  datasource:
    url: jdbc:mysql://localhost:3306/changxing_booking?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost
    port: 6379
    database: 2
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /

# Redisson分布式锁配置
redisson:
  single-server-config:
    address: "redis://127.0.0.1:6379"
    database: 2
    connection-minimum-idle-size: 10
    connection-pool-size: 64
```

---

## 8. 数据库规划

### 8.1 数据库拆分

| 数据库 | 对应服务 | 主要表 |
|--------|----------|--------|
| changxing_user | 用户服务 | users, admins |
| changxing_car | 车辆服务 | car_brands, car_series, car_trims, car_features, car_images, cars |
| changxing_booking | 订单服务 | bookings |
| changxing_store | 门店服务 | stores |
| changxing_review | 评价服务 | reviews |
| changxing_service | 客服服务 | conversations, conversation_messages |
| changxing_stats | 统计服务 | daily_stats |

### 8.2 跨服务数据同步

```
┌─────────────┐    MQ消息    ┌─────────────┐
│  订单服务    │────────────>│  车辆服务    │
│ (创建订单)   │             │ (更新状态)   │
└─────────────┘             └─────────────┘

┌─────────────┐    MQ消息    ┌─────────────┐
│  订单服务    │────────────>│  用户服务    │
│ (完成订单)   │             │ (更新积分)   │
└─────────────┘             └─────────────┘

┌─────────────┐    MQ消息    ┌─────────────┐
│  评价服务    │────────────>│  车辆服务    │
│ (新增评价)   │             │ (更新评分)   │
└─────────────┘             └─────────────┘
```

---

## 9. 认证服务设计

### 9.1 JWT Token 结构

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "1",
    "name": "张三",
    "phone": "13800138000",
    "role": "user",
    "memberLevel": "黄金会员",
    "iat": 1718544000,
    "exp": 1718630400
  }
}
```

### 9.2 Token 刷新策略

```
┌──────────────────────────────────────────────────────────────┐
│                        Token 生命周期                         │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  登录成功 ──► 生成 Access Token (2小时)                       │
│            ──► 生成 Refresh Token (7天)                       │
│                                                              │
│  Access Token 过期 ──► 使用 Refresh Token 刷新               │
│                                                              │
│  Refresh Token 过期 ──► 重新登录                             │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

---

## 10. 部署架构

### 11.1 Docker Compose

```yaml
version: '3.8'

services:
  # Nacos
  nacos:
    image: nacos/nacos-server:v2.3.0
    ports:
      - "8848:8848"
      - "9848:9848"
    environment:
      - MODE=standalone
    volumes:
      - nacos_data:/home/nacos/data

  # MySQL
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=${DB_PASSWORD}
      - MYSQL_DATABASE=changxing
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d

  # Redis
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  # RabbitMQ
  rabbitmq:
    image: rabbitmq:3.12-management
    ports:
      - "5672:5672"
      - "15672:15672"

  # MinIO
  minio:
    image: minio/minio
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      - MINIO_ROOT_USER=minioadmin
      - MINIO_ROOT_PASSWORD=minioadmin
    command: server /data --console-address ":9001"

  # Gateway
  gateway:
    build: ./changxing-gateway
    ports:
      - "8080:8080"
    depends_on:
      - nacos

  # Auth Service
  auth:
    build: ./changxing-auth
    depends_on:
      - nacos
      - redis

  # User Service
  user:
    build: ./changxing-user
    depends_on:
      - nacos
      - mysql
      - redis

  # Car Service
  car:
    build: ./changxing-car
    depends_on:
      - nacos
      - mysql

  # Booking Service
  booking:
    build: ./changxing-booking
    depends_on:
      - nacos
      - mysql
      - redis
      - rabbitmq

  # Store Service
  store:
    build: ./changxing-store
    depends_on:
      - nacos
      - mysql

volumes:
  nacos_data:
  mysql_data:
  redis_data:
```

### 11.2 服务端口规划

| 服务 | 端口 | 说明 |
|------|------|------|
| Gateway | 8080 | 对外入口 |
| Nacos | 8848 | 注册/配置中心 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672/15672 | 消息队列 |
| MinIO | 9000/9001 | 对象存储 |
| MinIO Console | 9001 | MinIO管理控制台 |
| Auth | 9000 | 认证服务 |
| User | 9001 | 用户服务 |
| Car | 9002 | 车辆服务 |
| Booking | 9003 | 订单服务 |
| Store | 9004 | 门店服务 |
| Review | 9005 | 评价服务 |
| Admin | 9006 | 管理服务 |
| Service | 9008 | 客服服务 |
| Upload | 9009 | 上传服务 |

---

## 12. 接口路由映射

### 12.1 用户端接口

| 原接口路径 | 路由后路径 | 目标服务 |
|-----------|-----------|----------|
| POST /auth/login | /api/auth/login | changxing-auth |
| POST /auth/register | /api/auth/register | changxing-auth |
| GET /user/profile | /api/user/profile | changxing-user |
| GET /cars | /api/cars | changxing-car |
| GET /cars/:id | /api/cars/:id | changxing-car |
| GET /bookings | /api/bookings | changxing-booking |
| POST /bookings | /api/bookings | changxing-booking |
| PUT /bookings/:id/cancel | /api/bookings/:id/cancel | changxing-booking |
| GET /stores | /api/stores | changxing-store |
| GET /reviews/trim/:id | /api/reviews/trim/:id | changxing-review |
| POST /service/conversations | /api/service/conversations | changxing-service |
| POST /service/conversations/:id/messages | /api/service/conversations/:id/messages | changxing-service |
| GET /service/conversations/:id | /api/service/conversations/:id | changxing-service |
| GET /service/conversations | /api/service/conversations | changxing-service |
| PUT /service/conversations/:id/close | /api/service/conversations/:id/close | changxing-service |

### 12.2 管理端接口

| 原接口路径 | 路由后路径 | 目标服务 |
|-----------|-----------|----------|
| POST /admin/login | /api/admin/login | changxing-admin |
| GET /admin/stats | /api/admin/stats | changxing-admin |
| GET /admin/cars | /api/admin/cars | changxing-admin |
| POST /admin/cars | /api/admin/cars | changxing-admin |
| PUT /admin/cars/:id/status | /api/admin/cars/:id/status | changxing-admin |
| DELETE /admin/cars/:id | /api/admin/cars/:id | changxing-admin |
| GET /admin/bookings | /api/admin/bookings | changxing-admin |
| GET /admin/users | /api/admin/users | changxing-admin |
| GET /admin/stores | /api/admin/stores | changxing-admin |
| PUT /admin/stores/:id/status | /api/admin/stores/:id/status | changxing-admin |
| POST /upload/image | /api/upload/image | changxing-upload |
| POST /upload/images | /api/upload/images | changxing-upload |
| DELETE /upload/image | /api/upload/image | changxing-upload |

---

## 13. 监控与日志

### 13.1 Spring Boot Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

### 13.2 日志规范

```yaml
logging:
  level:
    com.changxing: INFO
    org.springframework.cloud.gateway: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId}] %logger{50} - %msg%n"
  file:
    name: /var/log/changxing/${spring.application.name}.log
```

---

## 14. MinIO 对象存储

### 14.1 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                      MinIO 对象存储                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Bucket: changxing                                          │
│  ├── cars/                                                  │
│  │   ├── exterior/          # 车辆外观图片                    │
│  │   │   ├── car_20260623_143022.jpg                       │
│  │   │   └── car_20260623_143023.jpg                       │
│  │   └── interior/          # 车辆内饰图片                    │
│  │       ├── car_20260623_143024.jpg                       │
│  │       └── car_20260623_143025.jpg                       │
│  ├── avatar/                # 用户头像                       │
│  └── store/                 # 门店图片                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 14.2 MinIO 配置

**application.yml (公共模块)**:
```yaml
minio:
  endpoint: http://${MINIO_HOST:localhost}:${MINIO_PORT:9000}
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin}
  bucket: changxing
  region: cn-east-1
```

**Docker Compose**:
```yaml
  # MinIO
  minio:
    image: minio/minio:latest
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    command: server /data --console-address ":9001"
    volumes:
      - minio_data:/data
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### 14.3 MinIO 配置类

```java
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
    }
}
```

### 14.4 文件上传服务

```java
@Service
@Slf4j
public class UploadService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 上传图片
     * @param file MultipartFile
     * @param type 类型: exterior/interior/avatar
     * @return 文件URL
     */
    public UploadResult uploadImage(MultipartFile file, String type) {
        // 1. 校验文件类型
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException(400, "文件格式不支持，仅支持 JPG/PNG/WEBP");
        }

        // 2. 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(400, "文件大小超过限制，最大5MB");
        }

        // 3. 生成文件名
        String fileName = generateFileName(file.getOriginalFilename(), type);

        // 4. 上传到MinIO
        try {
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(500, "文件上传失败");
        }

        // 5. 返回结果
        String url = endpoint + "/" + bucket + "/" + fileName;
        return new UploadResult(url, fileName, file.getSize(), file.getContentType());
    }

    /**
     * 删除文件
     */
    public void deleteFile(String url) {
        try {
            String objectName = url.replace(endpoint + "/" + bucket + "/", "");
            minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
        } catch (Exception e) {
            log.error("文件删除失败", e);
        }
    }

    private String generateFileName(String originalName, String type) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String random = String.valueOf((int)(Math.random() * 10000));
        String extension = StringUtils.getFilenameExtension(originalName);
        return "cars/" + type + "/" + timestamp + "_" + random + "." + extension;
    }
}
```

### 14.5 上传控制器

```java
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @PostMapping("/image")
    public Result<UploadResult> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "exterior") String type) {
        return Result.success(uploadService.uploadImage(file, type));
    }

    @DeleteMapping("/image")
    public Result<Void> deleteImage(@RequestBody Map<String, String> params) {
        uploadService.deleteFile(params.get("url"));
        return Result.success();
    }
}
```

### 14.6 Bucket策略

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {"AWS": ["*"]},
      "Action": ["s3:GetObject"],
      "Resource": ["arn:aws:s3:::changxing/public/*"]
    }
  ]
}
```

---

## 15. 安全设计

### 14.1 接口权限

| 接口类型 | 权限要求 | 说明 |
|---------|---------|------|
| 公开接口 | 无需登录 | 车辆列表、门店列表、评价列表 |
| 用户接口 | 需要登录 | 订单、个人信息 |
| 管理接口 | 需要管理员权限 | 后台管理所有接口 |

### 14.2 敏感数据处理

```java
// 密码加密
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String encodedPassword = encoder.encode(rawPassword);

// 手机号脱敏
public static String maskPhone(String phone) {
    return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
}
```

---

## 16. 分布式锁与并发控制

### 15.1 问题场景

租车系统核心并发问题：**同一辆车在同一时间段内不能被多个用户同时下单**。

```
时间线:
用户A ──── 查询车辆可用 ────────── 创建订单 ─────►
用户B ──────── 查询车辆可用 ────────── 创建订单 ─────►
                    ↑ 此时车辆仍显示可用，导致重复下单
```

### 15.2 技术选型

| 方案 | 实现 | 优点 | 缺点 |
|------|------|------|------|
| MySQL悲观锁 | SELECT ... FOR UPDATE | 简单 | 性能差，高并发下阻塞 |
| MySQL乐观锁 | version字段 | 无锁 | 失败率高，需重试 |
| **Redis分布式锁** | **Redisson** | **高性能，可重入，自动续期** | 依赖Redis |
| Redis + Lua脚本 | RedisTemplate | 轻量 | 需自己实现续期、可重入 |

**推荐方案**: Redisson（基于Redis的Java分布式锁框架）

### 15.3 订单服务引入Redis

**pom.xml 依赖**:
```xml
<!-- Redisson -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.0</version>
</dependency>
```

**application.yml 配置**:
```yaml
server:
  port: 9003

spring:
  application:
    name: changxing-booking
  redis:
    host: localhost
    port: 6379
    database: 2  # 订单服务使用独立DB

redisson:
  single-server-config:
    address: "redis://127.0.0.1:6379"
    database: 2
    connection-minimum-idle-size: 10
    connection-pool-size: 64
```

### 15.4 锁Key设计

```
锁粒度: 车辆 + 时间段
锁Key格式: lock:booking:car:{carId}:{startDate}:{endDate}

示例: 用户预约 6月15日-6月18日 租用车辆ID=1
锁Key: lock:booking:car:1:2026-06-15:2026-06-18

锁超时: 10秒（防死锁）
等待时间: 3秒（超时失败返回）
```

### 15.5 核心代码实现

**BookingService.java**:
```java
@Service
@Slf4j
public class BookingService {

    @Autowired
    private BookingMapper bookingMapper;
    
    @Autowired
    private RedissonClient redissonClient;
    
    @Autowired
    private CarFeignClient carFeignClient;

    /**
     * 创建订单（带分布式锁）
     */
    @Transactional
    public Booking createBooking(CreateBookingRequest request) {
        // 1. 构造锁Key
        String lockKey = String.format("lock:booking:car:%d:%s:%s",
            request.getCarId(), request.getStartDate(), request.getEndDate());
        
        // 2. 获取分布式锁
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(409, "系统繁忙，请稍后重试");
            }
            
            // 3. 锁内再次检查车辆可用性（防止重复下单）
            if (!isCarAvailable(request.getCarId(), request.getStartDate(), request.getEndDate())) {
                throw new BusinessException(409, "该车辆在所选时间段已被预订");
            }
            
            // 4. 检查车辆状态
            CarDTO car = carFeignClient.getCarDetail(request.getCarId()).getData();
            if (car == null || !"available".equals(car.getStatus())) {
                throw new BusinessException(400, "车辆不可用");
            }
            
            // 5. 创建订单
            Booking booking = buildBooking(request);
            bookingMapper.insert(booking);
            
            // 6. 发送MQ消息，通知车辆服务更新状态
            rabbitTemplate.convertAndSend("booking.exchange", "booking.created", booking);
            
            log.info("订单创建成功: carId={}, userId={}, date={}~{}", 
                request.getCarId(), request.getUserId(), request.getStartDate(), request.getEndDate());
            
            return booking;
            
        } finally {
            // 7. 释放锁
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 检查车辆在指定时间段是否可用
     */
    private boolean isCarAvailable(Long carId, String startDate, String endDate) {
        // 查询该车辆在目标时间段是否有活跃订单
        int count = bookingMapper.countActiveBookings(carId, startDate, endDate);
        return count == 0;
    }
}
```

**BookingMapper.xml**:
```xml
<select id="countActiveBookings" resultType="int">
    SELECT COUNT(*) FROM bookings
    WHERE car_id = #{carId}
      AND status IN ('pending', 'confirmed', 'active')
      AND start_date &lt;= #{endDate}
      AND end_date &gt;= #{startDate}
</select>
```

### 15.6 锁类型对比

```java
// 方式1: tryLock（推荐）— 超时自动失败
RLock lock = redissonClient.getLock("lock:booking:car:1:2026-06-15:2026-06-18");
boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
// 3秒内最多等待获取锁，获取后10秒自动释放

// 方式2: lock（阻塞等待）— 不推荐用于Web请求
lock.lock(10, TimeUnit.SECONDS);

// 方式3: 多车辆批量锁（订单含多辆车时）
RLock lock1 = redissonClient.getLock("lock:booking:car:1:...");
RLock lock2 = redissonClient.getLock("lock:booking:car:2:...");
RLock multiLock = redissonClient.getMultiLock(lock1, lock2);
multiLock.tryLock(3, 10, TimeUnit.SECONDS);
```

### 15.7 完整下单流程

```
┌─────────────────────────────────────────────────────────────────┐
│                      创建订单时序图                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  用户A                Redis               MySQL                  │
│    │                    │                   │                    │
│    │  POST /bookings    │                   │                    │
│    │───────────────────>│                   │                    │
│    │                    │                   │                    │
│    │  1.获取分布式锁     │                   │                    │
│    │  tryLock(3,10)     │                   │                    │
│    │───────────────────>│                   │                    │
│    │<───────────────────│                   │                    │
│    │    locked=true     │                   │                    │
│    │                    │                   │                    │
│    │  2.查询冲突订单     │                   │                    │
│    │───────────────────────────────────────>│                    │
│    │<───────────────────────────────────────│                    │
│    │    count=0(可用)   │                   │                    │
│    │                    │                   │                    │
│    │  3.插入订单         │                   │                    │
│    │───────────────────────────────────────>│                    │
│    │<───────────────────────────────────────│                    │
│    │    success         │                   │                    │
│    │                    │                   │                    │
│    │  4.释放锁           │                   │                    │
│    │  unlock()          │                   │                    │
│    │───────────────────>│                   │                    │
│    │<───────────────────│                   │                    │
│    │                    │                   │                    │
│    │  5.返回订单         │                   │                    │
│    │<───────────────────│                   │                    │
│                                                                 │
│  注意: 用户B在用户A持有锁期间会等待或超时失败                        │
└─────────────────────────────────────────────────────────────────┘
```

### 15.8 异常处理与降级

```java
@Service
@Slf4j
public class BookingService {

    public Booking createBooking(CreateBookingRequest request) {
        String lockKey = String.format("lock:booking:car:%d:%s:%s",
            request.getCarId(), request.getStartDate(), request.getEndDate());
        
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 等待3秒，锁自动释放10秒
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                // 降级方案: 直接查库判断
                return createBookingWithoutLock(request);
            }
            
            // 正常加锁流程
            return doCreateBooking(request);
            
        } catch (RedisException e) {
            // Redis不可用降级: 直接走数据库
            log.warn("Redis不可用，降级为数据库查询", e);
            return createBookingWithoutLock(request);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 无锁降级方案: 依赖数据库唯一约束
     */
    private Booking createBookingWithoutLock(CreateBookingRequest request) {
        // 使用乐观锁或数据库唯一索引防重
        Booking booking = buildBooking(request);
        try {
            bookingMapper.insert(booking);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(409, "订单创建失败，请刷新后重试");
        }
        return booking;
    }
}
```

### 15.9 数据库防重索引

作为最后一道防线，在数据库层面添加防重索引：

```sql
-- 订单表添加防重索引: 同一辆车在同一时间段只能有一个活跃订单
ALTER TABLE bookings ADD UNIQUE INDEX idx_car_dates_unique (car_id, start_date, end_date, status)
    WHERE status IN ('pending', 'confirmed', 'active');

-- 或者使用普通索引 + 应用层控制
CREATE INDEX idx_bookings_car_dates ON bookings(car_id, start_date, end_date, status);
```

### 15.10 配置总结

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 锁Key | `lock:booking:car:{carId}:{start}:{end}` | 按车辆+日期段加锁 |
| 等待时间 | 3秒 | 获取锁最多等待3秒 |
| 持有时间 | 10秒 | 锁自动释放，防死锁 |
| Redis DB | 2 | 订单服务独立DB |
| 连接池 | 64 | 高并发支持 |
| 降级方案 | 直接查库 | Redis不可用时降级 |
