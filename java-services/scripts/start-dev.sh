#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo "   E-Commerce System - Dev Mode Startup   "
echo "   (No Docker Required)                   "
echo "=========================================="

cd "$PROJECT_DIR"

SERVICES=(
    "auth-service:8083"
    "order-service:8084"
    "purchase-service:8085"
    "product-service:8086"
    "platform-douyin:8087"
    "platform-xiaohongshu:8088"
    "platform-1688:8089"
    "api-gateway:8080"
)

check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "Warning: Port $port is already in use"
        return 1
    fi
    return 0
}

wait_for_service() {
    local port=$1
    local name=$2
    local max_wait=30
    local count=0
    
    echo "Waiting for $name to start on port $port..."
    while ! curl -s http://localhost:$port/actuator/health >/dev/null 2>&1; do
        sleep 1
        count=$((count + 1))
        if [ $count -ge $max_wait ]; then
            echo "Warning: $name may not have started properly"
            return 1
        fi
    done
    echo "$name is ready!"
    return 0
}

start_service() {
    local service=$1
    local port=$2
    local jar_path="$PROJECT_DIR/$service/target/$service.jar"
    
    if [ ! -f "$jar_path" ]; then
        echo "Building $service..."
        mvn -pl $service -am clean package -DskipTests -q
    fi
    
    check_port $port
    
    echo "Starting $service on port $port..."
    java -jar "$jar_path" --spring.profiles.active=dev > "$PROJECT_DIR/logs/$service.log" 2>&1 &
    echo $! > "$PROJECT_DIR/logs/$service.pid"
    
    sleep 2
    wait_for_service $port $service
}

mkdir -p "$PROJECT_DIR/logs"

for service_info in "${SERVICES[@]}"; do
    service=$(echo $service_info | cut -d: -f1)
    port=$(echo $service_info | cut -d: -f2)
    start_service "$service" "$port"
done

echo "=========================================="
echo "   All services started successfully!     "
echo "=========================================="
echo ""
echo "Service URLs:"
echo "  - API Gateway:    http://localhost:8080"
echo "  - Auth Service:   http://localhost:8083 (H2 Console: /h2-console)"
echo "  - Order Service:  http://localhost:8084"
echo "  - Purchase:       http://localhost:8085 (H2 Console: /h2-console)"
echo "  - Product:        http://localhost:8086 (H2 Console: /h2-console)"
echo "  - Platform Douyin:      http://localhost:8087"
echo "  - Platform Xiaohongshu: http://localhost:8088"
echo "  - Platform 1688:        http://localhost:8089"
echo ""
echo "H2 Console URLs (JDBC URL: jdbc:h2:mem:<dbname>):"
echo "  - Auth:    http://localhost:8083/h2-console"
echo "  - Purchase: http://localhost:8085/h2-console"
echo ""
echo "To stop all services: ./scripts/stop-dev.sh"
echo "Logs directory: $PROJECT_DIR/logs"