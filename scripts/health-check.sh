#!/bin/bash

# ============================================
# 快速健康检查脚本
# ============================================

echo "========================================"
echo "电商客服系统 - 健康检查"
echo "========================================"

# 服务列表
SERVICES=(
    "API网关:8080"
    "认证服务:8083"
    "订单服务:8081"
    "采购服务:8082"
    "商品服务:8084"
    "客服服务:8001"
    "抖音平台:8085"
    "小红书平台:8086"
    "1688平台:8087"
)

# 基础设施列表
INFRA=(
    "TiDB:4000"
    "Redis:7001"
    "Kafka:9092"
    "RabbitMQ:5672"
    "Nacos:8848"
    "Prometheus:9090"
    "Grafana:3000"
)

check_service() {
    local name=$1
    local port=$2
    
    if nc -z localhost $port 2>/dev/null; then
        echo -e "  \033[0;32m✓\033[0m $name (端口: $port)"
        return 0
    else
        echo -e "  \033[0;31m✗\033[0m $name (端口: $port) - 未运行"
        return 1
    fi
}

echo ""
echo ">>> 检查基础设施..."
INFRA_OK=0
INFRA_TOTAL=${#INFRA[@]}
for item in "${INFRA[@]}"; do
    name="${item%%:*}"
    port="${item##*:}"
    if check_service "$name" "$port"; then
        ((INFRA_OK++))
    fi
done

echo ""
echo ">>> 检查应用服务..."
SERVICES_OK=0
SERVICES_TOTAL=${#SERVICES[@]}
for item in "${SERVICES[@]}"; do
    name="${item%%:*}"
    port="${item##*:}"
    if check_service "$name" "$port"; then
        ((SERVICES_OK++))
    fi
done

echo ""
echo "========================================"
echo "检查结果"
echo "========================================"
echo ""
echo "基础设施: $INFRA_OK/$INFRA_TOTAL 运行中"
echo "应用服务: $SERVICES_OK/$SERVICES_TOTAL 运行中"
echo ""

if [ $SERVICES_OK -eq $SERVICES_TOTAL ]; then
    echo -e "\033[0;32m所有服务运行正常!\033[0m"
else
    echo -e "\033[0;33m部分服务未运行，请检查日志。\033[0m"
fi