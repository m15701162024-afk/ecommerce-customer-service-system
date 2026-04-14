#!/bin/bash
# 编译所有后端服务

SERVICES_DIR="/Users/youluzhineng/Desktop/ecommerce-customer-service-system/java-services"

SERVICES=(
  "common"
  "auth-service"
  "order-service"
  "purchase-service"
  "product-service"
  "crm-service"
  "after-sale-service"
  "work-order-service"
  "automation-service"
  "platform-douyin"
  "platform-xiaohongshu"
  "platform-1688"
)

echo "Building all services..."

# 先编译common模块
echo "Building common module..."
cd "$SERVICES_DIR/common"
mvn clean install -DskipTests -q

# 编译其他服务
for SERVICE in "${SERVICES[@]}"; do
  if [ "$SERVICE" != "common" ]; then
    echo "Building $SERVICE..."
    cd "$SERVICES_DIR/$SERVICE"
    mvn clean package -DskipTests -q
  fi
done

echo "All services built!"
