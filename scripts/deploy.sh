#!/bin/bash

# ============================================
# 电商客服系统 - 完整部署脚本
# 版本: v2.0.0
# ============================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_DIR="/Users/youluzhineng/Desktop/ecommerce-customer-service-system"
INFRA_DIR="$PROJECT_DIR/infrastructure/docker-compose"

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}电商客服系统 v2.0 部署脚本${NC}"
echo -e "${BLUE}========================================${NC}"

# 1. 环境检查
check_environment() {
    echo -e "\n${YELLOW}[1/7] 检查环境...${NC}"
    
    # 检查Docker
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}✗ Docker未安装${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Docker已安装: $(docker --version)${NC}"
    
    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        echo -e "${RED}✗ Docker Compose未安装${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Docker Compose已安装: $(docker-compose --version)${NC}"
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        echo -e "${YELLOW}⚠ Java未安装 (可选)${NC}"
    else
        echo -e "${GREEN}✓ Java已安装: $(java -version 2>&1 | head -n 1)${NC}"
    fi
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        echo -e "${YELLOW}⚠ Maven未安装 (可选)${NC}"
    else
        echo -e "${GREEN}✓ Maven已安装: $(mvn -version 2>&1 | head -n 1)${NC}"
    fi
    
    # 检查Node.js
    if ! command -v node &> /dev/null; then
        echo -e "${YELLOW}⚠ Node.js未安装 (可选)${NC}"
    else
        echo -e "${GREEN}✓ Node.js已安装: $(node --version)${NC}"
    fi
}

# 2. 创建必要的配置文件
create_config_files() {
    echo -e "\n${YELLOW}[2/7] 创建配置文件...${NC}"
    
    # 创建Redis配置目录
    mkdir -p "$INFRA_DIR/config/redis"
    mkdir -p "$INFRA_DIR/config/prometheus"
    mkdir -p "$INFRA_DIR/config/grafana/provisioning"
    mkdir -p "$INFRA_DIR/config/logstash"
    
    # 创建Redis配置文件
    for port in 7001 7002 7003; do
        cat > "$INFRA_DIR/config/redis/redis-$port.conf" << EOF
port $port
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 5000
appendonly yes
daemonize no
bind 0.0.0.0
protected-mode no
EOF
        echo -e "${GREEN}✓ 创建Redis配置: redis-$port.conf${NC}"
    done
    
    # 创建Prometheus配置
    cat > "$INFRA_DIR/config/prometheus/prometheus.yml" << 'EOF'
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
  
  - job_name: 'spring-actuator'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'host.docker.internal:8080'
        - 'host.docker.internal:8081'
        - 'host.docker.internal:8082'
        - 'host.docker.internal:8083'
        - 'host.docker.internal:8088'
        - 'host.docker.internal:8089'
        - 'host.docker.internal:8090'
        - 'host.docker.internal:8091'
EOF
    echo -e "${GREEN}✓ 创建Prometheus配置${NC}"
    
    # 创建Logstash配置
    cat > "$INFRA_DIR/config/logstash/logstash.conf" << 'EOF'
input {
  tcp {
    port => 5044
    codec => json_lines
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "ecommerce-logs-%{+YYYY.MM.dd}"
  }
}
EOF
    echo -e "${GREEN}✓ 创建Logstash配置${NC}"
}

# 3. 启动基础设施
start_infrastructure() {
    echo -e "\n${YELLOW}[3/7] 启动基础设施...${NC}"
    
    cd "$INFRA_DIR"
    
    # 启动Docker服务
    docker-compose up -d
    
    echo -e "${GREEN}✓ 基础设施启动完成${NC}"
    echo -e "\n等待服务就绪..."
    sleep 30
}

# 4. 初始化数据库
init_database() {
    echo -e "\n${YELLOW}[4/7] 初始化数据库...${NC}"
    
    # 等待TiDB就绪
    echo "等待TiDB启动..."
    for i in {1..30}; do
        if docker exec ecommerce-tidb mysql -uroot -e "SELECT 1" &>/dev/null; then
            echo -e "${GREEN}✓ TiDB已就绪${NC}"
            break
        fi
        echo "等待TiDB... ($i/30)"
        sleep 2
    done
    
    # 创建数据库
    docker exec -i ecommerce-tidb mysql -uroot << 'EOF'
CREATE DATABASE IF NOT EXISTS ecommerce;
CREATE DATABASE IF NOT EXISTS ecommerce_auth;
CREATE DATABASE IF NOT EXISTS nacos;
EOF
    
    echo -e "${GREEN}✓ 数据库创建完成${NC}"
    
    # 执行初始化脚本
    if [ -f "$PROJECT_DIR/sql/init.sql" ]; then
        docker exec -i ecommerce-tidb mysql -uroot ecommerce < "$PROJECT_DIR/sql/init.sql"
        echo -e "${GREEN}✓ 初始化脚本执行完成${NC}"
    fi
    
    # 执行扩展脚本
    if [ -f "$PROJECT_DIR/sql/extension.sql" ]; then
        docker exec -i ecommerce-tidb mysql -uroot ecommerce < "$PROJECT_DIR/sql/extension.sql"
        echo -e "${GREEN}✓ 扩展脚本执行完成${NC}"
    fi
}

# 5. 构建服务
build_services() {
    echo -e "\n${YELLOW}[5/7] 构建Java服务...${NC}"
    
    cd "$PROJECT_DIR/java-services"
    
    # 检查Maven是否可用
    if command -v mvn &> /dev/null; then
        mvn clean package -DskipTests -T 4
        echo -e "${GREEN}✓ Java服务构建完成${NC}"
    else
        echo -e "${YELLOW}⚠ Maven不可用，跳过构建${NC}"
        echo -e "${YELLOW}请手动运行: cd java-services && mvn clean package -DskipTests${NC}"
    fi
}

# 6. 构建前端
build_frontend() {
    echo -e "\n${YELLOW}[6/7] 构建前端...${NC}"
    
    cd "$PROJECT_DIR/frontend"
    
    if command -v npm &> /dev/null; then
        npm install
        npm run build
        echo -e "${GREEN}✓ 前端构建完成${NC}"
    else
        echo -e "${YELLOW}⚠ npm不可用，跳过构建${NC}"
        echo -e "${YELLOW}请手动运行: cd frontend && npm install && npm run build${NC}"
    fi
}

# 7. 显示状态
show_status() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${GREEN}部署完成！${NC}"
    echo -e "${BLUE}========================================${NC}"
    
    echo -e "\n${YELLOW}服务地址:${NC}"
    echo -e "  前端:        ${GREEN}http://localhost:5173${NC}"
    echo -e "  API网关:     ${GREEN}http://localhost:8080${NC}"
    echo -e "  认证服务:    ${GREEN}http://localhost:8083${NC}"
    echo -e "  工单服务:    ${GREEN}http://localhost:8088${NC}"
    echo -e "  售后服务:    ${GREEN}http://localhost:8089${NC}"
    echo -e "  自动化服务:  ${GREEN}http://localhost:8090${NC}"
    echo -e "  CRM服务:     ${GREEN}http://localhost:8091${NC}"
    
    echo -e "\n${YELLOW}管理界面:${NC}"
    echo -e "  Nacos:       ${GREEN}http://localhost:8848/nacos${NC}"
    echo -e "  RabbitMQ:    ${GREEN}http://localhost:15672${NC} (admin/admin123)"
    echo -e "  Grafana:     ${GREEN}http://localhost:3000${NC} (admin/admin123)"
    echo -e "  Prometheus:  ${GREEN}http://localhost:9090${NC}"
    echo -e "  Kibana:      ${GREEN}http://localhost:5601${NC}"
    echo -e "  MinIO:       ${GREEN}http://localhost:9001${NC} (minioadmin/minioadmin123)"
    
    echo -e "\n${YELLOW}数据库连接:${NC}"
    echo -e "  TiDB:        ${GREEN}localhost:4000${NC} (root/无密码)"
    echo -e "  Redis:       ${GREEN}localhost:7001-7003${NC}"
    echo -e "  Kafka:       ${GREEN}localhost:9092${NC}"
    
    echo -e "\n${YELLOW}下一步:${NC}"
    echo -e "  1. 启动Java服务: cd java-services && ./start-all.sh"
    echo -e "  2. 启动Python服务: cd python-services/customer-service && python -m uvicorn app.main:app --port 8001"
    echo -e "  3. 启动前端开发服务器: cd frontend && npm run dev"
    echo -e "  4. 访问前端: http://localhost:5173"
}

# 主函数
main() {
    check_environment
    create_config_files
    start_infrastructure
    init_database
    build_services
    build_frontend
    show_status
}

# 执行
main "$@"