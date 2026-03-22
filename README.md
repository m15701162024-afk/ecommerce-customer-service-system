# 全自动网店客服采购系统

## 项目概述

一个企业级的多平台电商自动化系统，支持抖音、淘宝(千牛)、小红书三大平台的智能客服和1688自动采购。

| 特性 | 说明 |
|------|------|
| **日订单处理能力** | 100万+ |
| **支持平台** | 抖音、淘宝(千牛)、小红书、1688 |
| **核心功能** | AI智能客服、自动采购下单、订单管理、风控对账 |
| **技术栈** | Java(Spring Boot) + Python(FastAPI) + TiDB + Redis + Kafka |

## 项目结构

```
ecommerce-customer-service-system/
├── java-services/                 # Java微服务模块
│   ├── api-gateway/              # Spring Cloud Gateway 网关
│   ├── auth-service/             # 认证授权服务
│   ├── user-service/             # 用户管理服务
│   ├── order-service/            # 订单管理服务
│   ├── purchase-service/         # 采购管理服务
│   ├── product-service/          # 商品管理服务
│   ├── payment-service/          # 支付管理服务
│   ├── risk-control-service/     # 风控服务
│   ├── reconciliation-service/   # 对账服务
│   ├── platform-douyin/          # 抖音平台对接
│   ├── platform-xiaohongshu/     # 小红书平台对接
│   ├── platform-1688/            # 1688平台对接
│   └── common/                   # 公共模块
│
├── python-services/              # Python服务模块
│   ├── qianniu-plugin/          # 千牛插件服务
│   ├── customer-service/        # AI客服服务
│   └── message-service/         # 消息推送服务
│
├── infrastructure/              # 基础设施配置
│   ├── docker-compose/         # Docker Compose配置
│   ├── kubernetes/             # K8s部署配置
│   ├── monitoring/             # 监控配置
│   └── logging/                # 日志配置
│
├── docs/                        # 项目文档
├── scripts/                     # 部署脚本
├── sql/                         # 数据库脚本
└── config/                      # 配置文件
```

## 技术架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         用户层                                   │
│   千牛客户端  │  抖音商家后台  │  小红书商家  │  1688商家后台       │
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                    API Gateway (Spring Cloud Gateway)            │
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                       业务服务层                                  │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │客服服务  │ │采购服务  │ │订单服务  │ │支付服务  │ │风控服务  │   │
│  │(Python) │ │ (Java)  │ │ (Java)  │ │ (Java)  │ │ (Java)  │   │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘   │
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                       消息队列层                                  │
│         Kafka (订单事件)  │  RabbitMQ (客服消息)                  │
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                       数据存储层                                  │
│     TiDB (主数据库)  │  Redis Cluster (缓存)  │  ES (搜索)        │
└─────────────────────────────────────────────────────────────────┘
```

## 快速开始

### 环境要求

- JDK 17+
- Python 3.11+
- Docker & Docker Compose
- Kubernetes 1.28+ (生产环境)
- Node.js 18+ (前端开发)

### 本地开发环境启动

```bash
# 1. 启动基础设施
cd infrastructure/docker-compose
docker-compose up -d

# 2. 初始化数据库
mysql -h 127.0.0.1 -P 4000 -u root < ../../sql/init.sql

# 3. 启动Java服务
cd ../../java-services
mvn clean install -DskipTests
java -jar api-gateway/target/api-gateway.jar

# 4. 启动Python服务
cd ../python-services/customer-service
pip install -r requirements.txt
uvicorn app.main:app --reload
```

## 开发阶段

| 阶段 | 时间 | 内容 |
|------|------|------|
| Phase 1 | 第1-2周 | 基础设施搭建 |
| Phase 2 | 第3-5周 | 核心框架与基础服务 |
| Phase 3 | 第5-7周 | 平台对接开发 |
| Phase 4 | 第7-10周 | 核心业务服务 |
| Phase 5 | 第10-12周 | 风控、对账与辅助服务 |
| Phase 6 | 第13-14周 | 性能优化与上线 |

## 文档

- [架构设计文档](docs/architecture/README.md)
- [API文档](docs/api/README.md)
- [部署指南](docs/deployment/README.md)

## License

MIT