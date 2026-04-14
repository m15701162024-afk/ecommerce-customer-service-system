#!/bin/bash
# 启动所有后端服务

SERVICES_DIR="/Users/youluzhineng/Desktop/ecommerce-customer-service-system/java-services"

# 服务列表: 服务名 端口
SERVICES=(
  "order-service 8084"
  "purchase-service 8085"
  "crm-service 8090"
  "after-sale-service 8091"
  "work-order-service 8092"
  "platform-douyin 8087"
  "platform-xiaohongshu 8088"
  "platform-1688 8089"
)

echo "Starting services..."

for service_port in "${SERVICES[@]}"; do
  set -- $service_port
  SERVICE=$1
  PORT=$2
  
  if [ -f "$SERVICES_DIR/$SERVICE/target/$SERVICE-*.jar" ] || [ -f "$SERVICES_DIR/$SERVICE/target/${SERVICE}-1.0.0-SNAPSHOT.jar" ]; then
    echo "Starting $SERVICE on port $PORT..."
    cd "$SERVICES_DIR/$SERVICE"
    
    # 找到jar文件
    JAR_FILE=$(ls target/*.jar 2>/dev/null | head -1)
    
    if [ -n "$JAR_FILE" ]; then
      nohup java -jar "$JAR_FILE" --spring.profiles.active=dev --server.port=$PORT > /tmp/${SERVICE}.log 2>&1 &
      echo "  $SERVICE started (PID: $!)"
    else
      echo "  $SERVICE jar not found, skipping..."
    fi
  else
    echo "  $SERVICE not built, run: cd $SERVICES_DIR/$SERVICE && mvn package -DskipTests"
  fi
done

echo ""
echo "All services started. Check logs in /tmp/{service-name}.log"
