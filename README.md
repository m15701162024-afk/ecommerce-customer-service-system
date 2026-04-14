# 全自动网店客服采购系统

> 版本: v2.0.0 | 发布日期: 2026-03-24

## 项目简介

全自动网店客服采购系统是一个支持抖音、淘宝(千牛)、小红书三大平台的智能客服系统，集成1688自动采购功能，实现从客户咨询到商品采购的全流程自动化。

| 项目 | 说明 |
|------|------|
| **技术栈** | Java(Spring Boot) + Python(FastAPI) + Vue 3 + TiDB + Redis + Kafka |
| **架构** | 微服务架构 + API网关 + 消息队列 |
| **核心功能** | 智能客服、自动采购、工单管理、售后处理、CRM客户管理 |

---

## 功能模块

### 🎯 核心业务

| 模块 | 说明 | 状态 |
|------|------|------|
| 智能客服 | AI意图识别、知识库、多轮对话 | ✅ |
| 自动采购 | 1688货源匹配、自动下单、智能风控 | ✅ |
| 订单管理 | 多平台订单同步、状态管理 | ✅ |
| 商品管理 | 商品信息、SKU管理、货源匹配 | ✅ |

### 🆕 v2.0 新增功能

| 模块 | 说明 | 状态 |
|------|------|------|
| 工单系统 | 工单创建、分配、流转、SLA时效 | ✅ NEW |
| 售后处理 | 退款、退货、换货全流程 | ✅ NEW |
| 自动化引擎 | 关键词自动回复、工作流编排 | ✅ NEW |
| 消息模板 | 快捷回复、团队模板、变量替换 | ✅ NEW |
| CRM客户管理 | 客户档案、标签、分组、画像 | ✅ NEW |
| 质检监控 | 质检规则、敏感词检测 | ✅ NEW |
| 数据分析 | 运营概览、客服统计、销售分析 | ✅ NEW |
| 会话评价 | 满意度评分、评价统计 | ✅ NEW |
| 排班绩效 | 排班计划、绩效统计 | ✅ NEW |
| 商品推荐 | 智能推荐、关联推荐 | ✅ NEW |

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Vue 3)                        │
│       API层 │ Pinia状态管理 │ 权限控制 │ 统一请求处理        │
├─────────────────────────────────────────────────────────────┤
│                      API网关 (Spring Cloud Gateway)          │
│        路由 │ 限流 │ 熔断 │ 认证 │ 日志                       │
├─────────────────────────────────────────────────────────────┤
│                        服务层 (微服务)                       │
├─────────────┬─────────────┬─────────────┬─────────────────────┤
│  认证服务   │  订单服务   │  采购服务   │    工单服务          │
│  (8083)    │  (8081)     │  (8082)     │    (8088)           │
├─────────────┼─────────────┼─────────────┼─────────────────────┤
│  商品服务   │  售后服务   │  自动化服务  │   CRM服务           │
│  (8084)    │  (8089)     │  (8090)     │   (8091)            │
├─────────────┴─────────────┴─────────────┴─────────────────────┤
│                      平台对接服务                             │
│     抖音平台(8085) │ 小红书平台(8086) │ 1688平台(8087)        │
├─────────────────────────────────────────────────────────────┤
│                    客服AI服务 (Python FastAPI)               │
│           意图识别 │ 知识库检索 │ 自动回复 (8001)             │
├─────────────────────────────────────────────────────────────┤
│                        基础设施层                             │
│   TiDB │ Redis Cluster │ Kafka │ Nacos │ Prometheus │ Grafana│
└─────────────────────────────────────────────────────────────┘
```

---

## 服务清单

| 服务名称 | 端口 | 说明 |
|----------|------|------|
| api-gateway | 8080 | API统一网关 |
| auth-service | 8083 | 认证授权服务 |
| order-service | 8081 | 订单管理服务 |
| purchase-service | 8082 | 采购管理服务 |
| product-service | 8084 | 商品管理服务 |
| **work-order-service** | 8088 | 工单管理服务 |
| **after-sale-service** | 8089 | 售后处理服务 |
| **automation-service** | 8090 | 自动化流程服务 |
| **crm-service** | 8091 | CRM客户管理服务 |
| platform-douyin | 8085 | 抖音平台对接 |
| platform-xiaohongshu | 8086 | 小红书平台对接 |
| platform-1688 | 8087 | 1688平台对接 |
| customer-service | 8001 | Python客服AI服务 |
| frontend | 5173 | Vue前端应用 |

---

## 技术栈

### 后端
- **Java 17** + Spring Boot 3.2.3
- **Spring Cloud** (Gateway, Nacos, OpenFeign)
- **MyBatis Plus** ORM框架
- **Resilience4j** 熔断限流
- **Kafka** 消息队列
- **Redis** 缓存

### 前端
- **Vue 3** + Vite
- **Element Plus** UI组件
- **Pinia** 状态管理
- **Axios** HTTP客户端

### 基础设施
- **TiDB** 分布式数据库
- **Redis Cluster** 缓存集群
- **Kafka** 消息队列
- **Nacos** 服务注册/配置中心
- **Prometheus + Grafana** 监控

---

## 快速开始

### 环境要求
- Java 17+
- Maven 3.8+
- Python 3.9+
- Node.js 18+
- Docker & Docker Compose

### 启动步骤

```bash
# 1. 克隆项目
cd /path/to/ecommerce-customer-service-system

# 2. 启动基础设施
cd infrastructure/docker-compose
docker-compose up -d

# 3. 初始化数据库
mysql -h localhost -P 4000 -u root < sql/init.sql
mysql -h localhost -P 4000 -u root < sql/extension.sql

# 4. 配置环境变量
export JWT_SECRET=your_secret_key
export DB_PASSWORD=your_db_password

# 5. 启动Java服务
cd java-services
mvn clean install -DskipTests
# 启动各服务...

# 6. 启动Python服务
cd python-services/customer-service
pip install -r requirements.txt
uvicorn app.main:app --port 8001

# 7. 启动前端
cd frontend
npm install
npm run dev
```

### 访问地址
- 前端: http://localhost:5173
- API网关: http://localhost:8080
- Nacos控制台: http://localhost:8848/nacos
- Grafana: http://localhost:3000

---

## 目录结构

```
ecommerce-customer-service-system/
├── java-services/              # Java微服务
│   ├── api-gateway/           # API网关
│   ├── auth-service/          # 认证服务
│   ├── order-service/         # 订单服务
│   ├── purchase-service/      # 采购服务
│   ├── product-service/       # 商品服务
│   ├── work-order-service/    # 工单服务 ✨NEW
│   ├── after-sale-service/    # 售后服务 ✨NEW
│   ├── automation-service/    # 自动化服务 ✨NEW
│   ├── crm-service/           # CRM服务 ✨NEW
│   ├── platform-douyin/       # 抖音平台
│   ├── platform-xiaohongshu/  # 小红书平台
│   ├── platform-1688/         # 1688平台
│   └── common/                # 公共模块
├── python-services/            # Python服务
│   └── customer-service/      # 客服AI服务
├── frontend/                   # Vue前端
│   └── src/
│       ├── api/               # API接口
│       ├── views/             # 页面组件
│       ├── stores/            # 状态管理
│       └── router/            # 路由配置
├── sql/                        # 数据库脚本
│   ├── init.sql               # 初始化脚本
│   └── extension.sql          # 扩展脚本 ✨NEW
├── infrastructure/             # 基础设施
│   └── docker-compose/        # Docker配置
├── scripts/                    # 脚本工具
├── tests/                      # 测试文件
├── docs/                       # 文档
├── README.md                   # 项目说明
└── RELEASE_NOTES.md           # 发布说明 ✨NEW
```

---

## 功能完整度

| 维度 | v1.0 | v2.0 |
|------|------|------|
| 核心业务 | 75% | 95% |
| 客服功能 | 60% | 90% |
| 运营工具 | 40% | 85% |
| 数据分析 | 30% | 80% |
| **总体完整度** | **60%** | **95%** |

---

## 更新日志

### v2.0.0 (2026-03-24)
- ✨ 新增工单系统
- ✨ 新增售后处理系统
- ✨ 新增自动化流程引擎
- ✨ 新增CRM客户管理
- ✨ 新增质检监控
- ✨ 新增数据分析报表
- ✨ 新增会话评价
- ✨ 新增客服排班绩效
- ✨ 新增商品智能推荐
- ✨ 新增消息模板系统
- 🔧 优化API网关配置
- 🔧 增强安全配置

---

## License

MIT License