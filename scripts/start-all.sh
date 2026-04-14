#!/bin/bash

# ============================================
# 电商客服系统 - 服务启动脚本
# ============================================

set -e

PROJECT_DIR="/Users/youluzhineng/Desktop/ecommerce-customer-service-system"

echo "========================================"
echo "电商客服系统 - 服务启动"
echo "========================================"

# 检查Java版本
check_java() {
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        echo "✓ Java 版本: $(java -version 2>&1 | head -n 1)"
        return 0
    else
        echo "✗ Java 未安装"
        return 1
    fi
}

# 检查Python版本
check_python() {
    if command -v python3 &> /dev/null; then
        echo "✓ Python 版本: $(python3 --version)"
        return 0
    else
        echo "✗ Python3 未安装"
        return 1
    fi
}

# 检查Node.js版本
check_node() {
    if command -v node &> /dev/null; then
        echo "✓ Node.js 版本: $(node --version)"
        return 0
    else
        echo "✗ Node.js 未安装"
        return 1
    fi
}

# 检查Maven
check_maven() {
    if command -v mvn &> /dev/null; then
        echo "✓ Maven 版本: $(mvn -version 2>&1 | head -n 1)"
        return 0
    else
        echo "✗ Maven 未安装"
        return 1
    fi
}

# 启动基础设施
start_infrastructure() {
    echo ""
    echo ">>> 启动基础设施..."
    cd "$PROJECT_DIR/infrastructure/docker-compose"
    
    if command -v docker-compose &> /dev/null; then
        docker-compose up -d
        echo "✓ 基础设施启动成功"
        echo "  - TiDB: localhost:4000"
        echo "  - Redis: localhost:7001-7003"
        echo "  - Kafka: localhost:9092"
        echo "  - RabbitMQ: localhost:5672 (管理界面: localhost:15672)"
        echo "  - Nacos: localhost:8848"
        echo "  - Prometheus: localhost:9090"
        echo "  - Grafana: localhost:3000"
    else
        echo "✗ docker-compose 未安装，跳过基础设施启动"
    fi
}

# 启动Java服务
start_java_services() {
    echo ""
    echo ">>> 启动Java服务..."
    cd "$PROJECT_DIR/java-services"
    
    # 启动认证服务
    echo "  启动认证服务 (端口: 8083)..."
    cd auth-service
    mvn spring-boot:run -DskipTests &
    cd ..
    
    sleep 5
    
    # 启动订单服务
    echo "  启动订单服务 (端口: 8081)..."
    cd order-service
    mvn spring-boot:run -DskipTests &
    cd ..
    
    sleep 3
    
    # 启动采购服务
    echo "  启动采购服务 (端口: 8082)..."
    cd purchase-service
    mvn spring-boot:run -DskipTests &
    cd ..
    
    sleep 3
    
    # 启动商品服务
    echo "  启动商品服务 (端口: 8084)..."
    cd product-service
    mvn spring-boot:run -DskipTests &
    cd ..
    
    sleep 3
    
    # 启动平台服务
    echo "  启动抖音平台服务 (端口: 8085)..."
    cd platform-douyin
    mvn spring-boot:run -DskipTests &
    cd ..
    
    echo "  启动小红书平台服务 (端口: 8086)..."
    cd platform-xiaohongshu
    mvn spring-boot:run -DskipTests &
    cd ..
    
    echo "  启动1688平台服务 (端口: 8087)..."
    cd platform-1688
    mvn spring-boot:run -DskipTests &
    cd ..
    
    sleep 5
    
    # 启动API网关
    echo "  启动API网关 (端口: 8080)..."
    cd api-gateway
    mvn spring-boot:run -DskipTests &
    cd ..
    
    echo "✓ Java服务启动完成"
}

# 启动Python服务
start_python_services() {
    echo ""
    echo ">>> 启动Python客服服务..."
    cd "$PROJECT_DIR/python-services/customer-service"
    
    # 安装依赖
    if [ -f "requirements.txt" ]; then
        pip3 install -r requirements.txt -q
    fi
    
    # 启动服务
    echo "  启动客服服务 (端口: 8001)..."
    python3 -m uvicorn app.main:app --host 0.0.0.0 --port 8001 &
    
    echo "✓ Python服务启动完成"
}

# 启动前端
start_frontend() {
    echo ""
    echo ">>> 启动前端..."
    cd "$PROJECT_DIR/frontend"
    
    # 安装依赖
    if [ -f "package.json" ]; then
        npm install --silent
    fi
    
    # 启动开发服务器
    echo "  启动前端开发服务器 (端口: 5173)..."
    npm run dev &
    
    echo "✓ 前端启动完成"
}

# 显示服务状态
show_status() {
    echo ""
    echo "========================================"
    echo "服务启动完成!"
    echo "========================================"
    echo ""
    echo "服务列表:"
    echo "  - API网关:     http://localhost:8080"
    echo "  - 认证服务:    http://localhost:8083"
    echo "  - 订单服务:    http://localhost:8081"
    echo "  - 采购服务:    http://localhost:8082"
    echo "  - 商品服务:    http://localhost:8084"
    echo "  - 客服服务:    http://localhost:8001"
    echo "  - 抖音平台:    http://localhost:8085"
    echo "  - 小红书平台:  http://localhost:8086"
    echo "  - 1688平台:    http://localhost:8087"
    echo "  - 前端:        http://localhost:5173"
    echo ""
    echo "管理界面:"
    echo "  - Nacos:       http://localhost:8848/nacos"
    echo "  - RabbitMQ:    http://localhost:15672 (admin/admin123)"
    echo "  - Grafana:     http://localhost:3000 (admin/admin123)"
    echo "  - Prometheus:  http://localhost:9090"
    echo ""
}

# 主函数
main() {
    echo ""
    echo "检查环境..."
    check_java
    check_python
    check_node
    check_maven
    echo ""
    
    # 选择启动模式
    echo "请选择启动模式:"
    echo "  1) 启动全部服务"
    echo "  2) 仅启动基础设施"
    echo "  3) 仅启动Java服务"
    echo "  4) 仅启动Python服务"
    echo "  5) 仅启动前端"
    echo "  0) 退出"
    echo ""
    read -p "请输入选项: " choice
    
    case $choice in
        1)
            start_infrastructure
            sleep 10
            start_java_services
            start_python_services
            start_frontend
            ;;
        2)
            start_infrastructure
            ;;
        3)
            start_java_services
            ;;
        4)
            start_python_services
            ;;
        5)
            start_frontend
            ;;
        0)
            echo "退出"
            exit 0
            ;;
        *)
            echo "无效选项"
            exit 1
            ;;
    esac
    
    show_status
}

main "$@"