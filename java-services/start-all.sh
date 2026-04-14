#!/bin/bash

# 一键启动所有服务
# 使用方法: ./start-all.sh

set -e

export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

cd "$(dirname "$0")"
mkdir -p logs

echo "=== 检查Java环境 ==="
java -version

echo ""
echo "=== 编译所有服务 ==="
mvn clean package -DskipTests -Dmaven.test.skip=true 2>&1 | tail -5

echo ""
echo "=== 停止旧服务 ==="
pkill -f "api-gateway\|auth-service\|order-service\|product-service\|purchase-service" 2>/dev/null || true
sleep 2

echo ""
echo "=== 启动服务 ==="

# 启动auth-service
echo "启动 auth-service (8083)..."
nohup java -jar auth-service/target/auth-service-1.0.0.jar \
    --spring.profiles.active=dev \
    --server.port=8083 \
    --spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration \
    > logs/auth-service.log 2>&1 &

sleep 5

# 启动order-service
echo "启动 order-service (8084)..."
nohup java -jar order-service/target/order-service-1.0.0-SNAPSHOT.jar \
    --spring.profiles.active=dev \
    --server.port=8084 \
    > logs/order-service.log 2>&1 &

sleep 3

# 启动product-service
echo "启动 product-service (8086)..."
nohup java -jar product-service/target/product-service-1.0.0-SNAPSHOT.jar \
    --spring.profiles.active=dev \
    --server.port=8086 \
    > logs/product-service.log 2>&1 &

sleep 3

# 启动purchase-service
echo "启动 purchase-service (8085)..."
nohup java -jar purchase-service/target/purchase-service-1.0.0-SNAPSHOT.jar \
    --spring.profiles.active=dev \
    --server.port=8085 \
    > logs/purchase-service.log 2>&1 &

sleep 3

# 启动api-gateway (最后启动)
echo "启动 api-gateway (8080)..."
nohup java -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar \
    --spring.profiles.active=dev \
    --server.port=8080 \
    > logs/api-gateway.log 2>&1 &

sleep 5

echo ""
echo "=== 检查服务状态 ==="
sleep 5

echo "API网关: $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/actuator/health)"
echo "认证服务: $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8083/actuator/health)"
echo "订单服务: $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8084/actuator/health)"
echo "商品服务: $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8086/actuator/health)"

echo ""
echo "=== 测试登录 ==="
curl -s -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}'

echo ""
echo ""
echo "=== 服务地址 ==="
echo "前端: http://localhost:3000"
echo "API网关: http://localhost:8080"
echo "认证服务: http://localhost:8083"