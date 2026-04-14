# 测试文档

## 测试概览

本文档描述了电商客服系统的测试流程和预期结果。

## 测试环境要求

### 必需软件
- Java 17+
- Maven 3.8+
- Python 3.9+
- Node.js 18+
- Docker & Docker Compose

### 端口要求
| 服务 | 端口 |
|------|------|
| API网关 | 8080 |
| 认证服务 | 8083 |
| 订单服务 | 8081 |
| 采购服务 | 8082 |
| 商品服务 | 8084 |
| 客服服务 | 8001 |
| 抖音平台 | 8085 |
| 小红书平台 | 8086 |
| 1688平台 | 8087 |
| 前端 | 5173 |

---

## 步骤1: 单元测试

### 1.1 采购服务业务逻辑测试

**测试文件**: `tests/test_purchase_service.py`

**测试用例**:
| 测试项 | 输入 | 预期输出 |
|--------|------|----------|
| 金额<1000元自动支付 | unit_price=50, quantity=10 | status=AUTO_PAID |
| 金额1000-10000需确认 | unit_price=100, quantity=15 | status=CONFIRMING |
| 金额>10000元预警 | unit_price=200, quantity=60 | status=WARNING |
| 确认订单成功 | status=CONFIRMING | status=COMPLETED |
| 确认已完成订单报错 | status=COMPLETED | 抛出异常 |
| 拒绝订单成功 | status=CONFIRMING | status=REJECTED |
| 取消订单成功 | status=CONFIRMING | status=CANCELLED |
| 取消已完成订单报错 | status=COMPLETED | 抛出异常 |

**运行命令**:
```bash
python3 tests/test_purchase_service.py
```

**预期输出**:
```
============================================================
采购服务业务逻辑测试
============================================================

[测试1] 金额小于1000元 - 应自动支付
  ✅ 通过: 金额=500.00, 状态=AUTO_PAID

[测试2] 金额1000-10000元 - 需人工确认
  ✅ 通过: 金额=1500.00, 状态=CONFIRMING

[测试3] 金额超过10000元 - 预警状态
  ✅ 通过: 金额=12000.00, 状态=WARNING

...

============================================================
测试结果: 通过=8, 失败=0, 总计=8
============================================================
```

### 1.2 Java单元测试

**测试文件**:
- `java-services/purchase-service/src/test/java/.../PurchaseServiceTest.java`
- `java-services/auth-service/src/test/java/.../AuthServiceTest.java`

**运行命令**:
```bash
cd java-services/purchase-service
mvn test

cd ../auth-service
mvn test
```

---

## 步骤2: 启动服务进行集成测试

### 2.1 启动基础设施

```bash
cd infrastructure/docker-compose
docker-compose up -d
```

**验证**:
```bash
docker-compose ps
```

**预期结果**: 所有服务状态为 `Up`

### 2.2 启动应用服务

**方式一: 使用启动脚本**
```bash
chmod +x scripts/start-all.sh
./scripts/start-all.sh
# 选择 1) 启动全部服务
```

**方式二: 手动启动**
```bash
# Java服务
cd java-services/auth-service && mvn spring-boot:run &
cd java-services/order-service && mvn spring-boot:run &
cd java-services/purchase-service && mvn spring-boot:run &
cd java-services/product-service && mvn spring-boot:run &
cd java-services/api-gateway && mvn spring-boot:run &
cd java-services/platform-douyin && mvn spring-boot:run &
cd java-services/platform-xiaohongshu && mvn spring-boot:run &
cd java-services/platform-1688 && mvn spring-boot:run &

# Python服务
cd python-services/customer-service
pip install -r requirements.txt
uvicorn app.main:app --port 8001 &

# 前端
cd frontend
npm install
npm run dev &
```

### 2.3 验证服务状态

```bash
chmod +x scripts/health-check.sh
./scripts/health-check.sh
```

**预期输出**:
```
>>> 检查基础设施...
  ✓ TiDB (端口: 4000)
  ✓ Redis (端口: 7001)
  ✓ Kafka (端口: 9092)
  ✓ RabbitMQ (端口: 5672)
  ✓ Nacos (端口: 8848)
  ✓ Prometheus (端口: 9090)
  ✓ Grafana (端口: 3000)

>>> 检查应用服务...
  ✓ API网关 (端口: 8080)
  ✓ 认证服务 (端口: 8083)
  ✓ 订单服务 (端口: 8081)
  ✓ 采购服务 (端口: 8082)
  ✓ 商品服务 (端口: 8084)
  ✓ 客服服务 (端口: 8001)
  ✓ 抖音平台 (端口: 8085)
  ✓ 小红书平台 (端口: 8086)
  ✓ 1688平台 (端口: 8087)

所有服务运行正常!
```

---

## 步骤3: API端点测试

### 3.1 运行API测试脚本

```bash
chmod +x scripts/test-api.sh
./scripts/test-api.sh
```

### 3.2 手动API测试

#### 认证服务

**登录**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "username": "admin",
    "role": "ADMIN"
  }
}
```

**注册**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"password123","email":"user@example.com"}'
```

#### 订单服务

**获取订单列表**
```bash
curl -X GET "http://localhost:8080/api/v1/orders/list?page=1&size=10" \
  -H "Authorization: Bearer <token>"
```

**订单同步**
```bash
curl -X POST http://localhost:8080/api/v1/orders/sync \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"platform":"DOUYIN"}'
```

#### 采购服务

**创建采购订单**
```bash
curl -X POST http://localhost:8080/api/v1/purchase/create \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "supplierName": "测试供应商",
    "productName": "测试商品",
    "quantity": 10,
    "unitPrice": 50.00
  }'
```
**预期响应**: 
- 金额500元 → 状态 AUTO_PAID
- 金额1500元 → 状态 CONFIRMING
- 金额12000元 → 状态 WARNING

**确认采购**
```bash
curl -X POST http://localhost:8080/api/v1/purchase/1/confirm \
  -H "Authorization: Bearer <token>"
```

**拒绝采购**
```bash
curl -X POST http://localhost:8080/api/v1/purchase/1/reject \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"reason":"价格不合理"}'
```

#### 客服服务

**发送消息**
```bash
curl -X POST http://localhost:8080/api/v1/chat/send \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "session_id": "session-001",
    "message": "你好，有什么可以帮助我的？",
    "buyer_id": "buyer-001"
  }'
```

**转人工**
```bash
curl -X POST http://localhost:8080/api/v1/chat/sessions/session-001/transfer \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"reason":"用户请求转人工"}'
```

---

## 测试结果汇总

### 功能测试清单

| 功能模块 | 测试项 | 状态 |
|----------|--------|------|
| 认证服务 | 用户登录 | ✅ |
| 认证服务 | 用户注册 | ✅ |
| 认证服务 | Token验证 | ✅ |
| 订单服务 | 获取列表 | ✅ |
| 订单服务 | 订单同步 | ✅ |
| 采购服务 | 创建订单(自动支付) | ✅ |
| 采购服务 | 创建订单(需确认) | ✅ |
| 采购服务 | 创建订单(预警) | ✅ |
| 采购服务 | 确认采购 | ✅ |
| 采购服务 | 拒绝采购 | ✅ |
| 商品服务 | 获取列表 | ✅ |
| 商品服务 | 创建商品 | ✅ |
| 客服服务 | 发送消息 | ✅ |
| 客服服务 | 转人工 | ✅ |
| 平台服务 | 抖音回调验证 | ✅ |
| 平台服务 | 小红书回调验证 | ✅ |
| 平台服务 | 1688回调验证 | ✅ |

---

## 问题排查

### 常见问题

1. **服务启动失败**
   - 检查端口是否被占用
   - 检查环境变量配置
   - 查看日志文件

2. **数据库连接失败**
   - 确认TiDB已启动
   - 检查数据库连接配置
   - 验证用户名密码

3. **API返回401**
   - 检查Token是否有效
   - 确认Authorization头格式正确

4. **Kafka连接失败**
   - 确认Kafka已启动
   - 检查bootstrap-servers配置