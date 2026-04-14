#!/bin/bash

# 电商客服系统 - 服务启动脚本
# 使用方法: ./start-services.sh [all|gateway|auth|order|product|purchase]

cd "$(dirname "$0")"

# 创建日志目录
mkdir -p logs

# 检查Java环境
check_java() {
    if ! command -v java &> /dev/null; then
        echo "❌ Java未安装或未配置到PATH"
        echo "请安装Java 17+ 或配置JAVA_HOME"
        exit 1
    fi
    echo "✅ Java环境: $(java -version 2>&1 | head -1)"
}

# 启动单个服务
start_service() {
    local name=$1
    local jar=$2
    local port=$3
    local profile=${4:-dev}
    
    if pgrep -f "$jar" > /dev/null; then
        echo "⚠️  $name 已在运行"
        return
    fi
    
    echo "🚀 启动 $name (端口: $port)..."
    nohup java -jar "$jar" \
        --spring.profiles.active=$profile \
        --server.port=$port \
        > logs/$name.log 2>&1 &
    
    sleep 2
    if pgrep -f "$jar" > /dev/null; then
        echo "✅ $name 启动成功"
    else
        echo "❌ $name 启动失败，请检查日志: logs/$name.log"
    fi
}

# 停止服务
stop_service() {
    local name=$1
    echo "🛑 停止 $name..."
    pkill -f "$name" 2>/dev/null
}

# 主逻辑
case "$1" in
    stop)
        echo "停止所有服务..."
        stop_service "api-gateway"
        stop_service "auth-service"
        stop_service "order-service"
        stop_service "product-service"
        stop_service "purchase-service"
        stop_service "platform-douyin"
        ;;
    gateway)
        check_java
        start_service "api-gateway" "api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar" 8080
        ;;
    auth)
        check_java
        start_service "auth-service" "auth-service/target/auth-service-1.0.0.jar" 8083
        ;;
    order)
        check_java
        start_service "order-service" "order-service/target/order-service-1.0.0-SNAPSHOT.jar" 8084
        ;;
    product)
        check_java
        start_service "product-service" "product-service/target/product-service-1.0.0-SNAPSHOT.jar" 8086
        ;;
    purchase)
        check_java
        start_service "purchase-service" "purchase-service/target/purchase-service-1.0.0-SNAPSHOT.jar" 8085
        ;;
    status)
        echo "服务状态:"
        pgrep -fl "api-gateway\|auth-service\|order-service\|product-service\|purchase-service" || echo "无运行中的服务"
        ;;
    all|"")
        check_java
        echo "=== 启动所有服务 ==="
        start_service "auth-service" "auth-service/target/auth-service-1.0.0.jar" 8083
        start_service "order-service" "order-service/target/order-service-1.0.0-SNAPSHOT.jar" 8084
        start_service "product-service" "product-service/target/product-service-1.0.0-SNAPSHOT.jar" 8086
        start_service "purchase-service" "purchase-service/target/purchase-service-1.0.0-SNAPSHOT.jar" 8085
        sleep 3
        start_service "api-gateway" "api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar" 8080
        echo ""
        echo "=== 服务地址 ==="
        echo "前端: http://localhost:3000"
        echo "API网关: http://localhost:8080"
        echo "认证服务: http://localhost:8083"
        ;;
    *)
        echo "用法: $0 [all|gateway|auth|order|product|purchase|status|stop]"
        ;;
esac