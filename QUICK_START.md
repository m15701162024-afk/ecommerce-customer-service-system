# 🚀 快速启动指南

## 问题：系统无法登录

**原因**：后端服务Redis依赖问题

## 解决方案

### 方案1：安装Redis（推荐）

```bash
# 安装Redis
brew install redis

# 启动Redis
brew services start redis

# 验证
redis-cli ping
# 应该返回: PONG
```

然后运行启动脚本：
```bash
cd ~/Desktop/ecommerce-customer-service-system/java-services
./start-all.sh
```

### 方案2：使用前端Mock数据

修改前端配置，直接使用Mock数据，绕过后端：

```bash
# 编辑 frontend/src/api/request.js
# 在 baseURL 前添加 Mock 判断
```

### 方案3：一键重启（修复后）

```bash
cd ~/Desktop/ecommerce-customer-service-system/java-services

# 设置Java环境
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 停止所有Java服务
pkill -f "java -jar"

# 启动Redis
brew services start redis

# 等待Redis启动
sleep 3

# 编译所有服务
mvn clean package -DskipTests

# 启动所有服务
nohup java -jar auth-service/target/auth-service-1.0.0.jar --spring.profiles.active=dev --server.port=8083 > logs/auth-service.log 2>&1 &
nohup java -jar order-service/target/order-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev --server.port=8084 > logs/order-service.log 2>&1 &
nohup java -jar product-service/target/product-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev --server.port=8086 > logs/product-service.log 2>&1 &
nohup java -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev --server.port=8080 > logs/api-gateway.log 2>&1 &

# 等待服务启动
sleep 15

# 测试登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 3000 | Vue应用 |
| API网关 | 8080 | 所有请求入口 |
| 认证服务 | 8083 | 登录/注册 |
| 订单服务 | 8084 | 订单管理 |
| 商品服务 | 8086 | 商品管理 |
| Redis | 6379 | 缓存服务 |

## 默认账号

- 用户名: `admin`
- 密码: `admin123`

## 故障排查

1. **503/502错误** - 后端服务未启动或Redis未运行
2. **401错误** - 登录接口正常，说明需要登录
3. **500错误** - 后端代码错误，查看日志

查看日志：
```bash
tail -100 ~/Desktop/ecommerce-customer-service-system/java-services/logs/auth-service.log
```