# 网店客服采购系统 - 部署运营指南

## 一、系统架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                        用户访问层                                 │
│         http://your-domain.com (前端管理界面)                     │
└─────────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────────┐
│                        API网关层                                  │
│              Spring Cloud Gateway (端口: 8080)                   │
└─────────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────────┐
│                        业务服务层                                 │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐            │
│  │抖音对接  │ │千牛插件  │ │采购服务  │ │客服服务  │            │
│  │(Java)   │ │(Python) │ │(Java)   │ │(Python) │            │
│  │端口:8081 │ │端口:8001 │ │端口:8082 │ │端口:8000 │            │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘            │
└─────────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────────┐
│                        基础设施层                                 │
│   TiDB(4000)  Redis(7001-7003)  Kafka(9092)  RabbitMQ(5672)    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 二、运营前准备清单

### 2.1 平台API权限申请（必须）

| 平台 | 申请入口 | 所需资质 | 审批周期 | 获取内容 |
|------|----------|----------|----------|----------|
| **抖音/抖店** | op.jinritemai.com | 营业执照+店铺 | 3-7天 | AppID, AppSecret |
| **淘宝/千牛** | open.taobao.com | 企业开发者+软著 | 7-14天 | AppKey, AppSecret |
| **小红书** | open.xiaohongshu.com | 企业认证 | 5-10天 | AppID, AppSecret |
| **1688** | open.1688.com | 企业+诚信通会员 | 3-5天 | AppKey, AppSecret |

### 2.2 服务器配置要求

**生产环境推荐配置：**

| 组件 | 配置 | 数量 | 用途 |
|------|------|------|------|
| 应用服务器 | 4核8G | 3台 | 运行业务服务 |
| 数据库服务器 | 8核16G | 3台 | TiDB集群 |
| Redis服务器 | 4核8G | 3台 | 缓存集群 |
| Kafka服务器 | 4核8G | 3台 | 消息队列 |

**最低开发环境：** 1台 8核16G 云服务器

### 2.3 域名与SSL证书

1. 准备域名（如：admin.yourshop.com）
2. 申请SSL证书（推荐Let's Encrypt免费证书）
3. 配置域名解析到服务器IP

---

## 三、部署步骤

### 3.1 基础设施部署

```bash
# 1. 克隆项目
cd /opt
git clone <your-repo> ecommerce-system
cd ecommerce-system

# 2. 启动基础设施（Docker）
cd infrastructure/docker-compose

# 创建配置目录
mkdir -p config/redis config/prometheus config/logstash

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 等待服务启动（约2-3分钟）
sleep 180

# 初始化数据库
mysql -h 127.0.0.1 -P 4000 -u root < ../../sql/init.sql
```

### 3.2 配置API密钥

```bash
# 创建环境变量文件
cat > .env << 'EOF'
# 抖音配置
DOUYIN_APP_ID=your_douyin_appid
DOUYIN_APP_SECRET=your_douyin_secret
DOUYIN_CALLBACK_URL=https://your-domain.com/api/v1/platform/douyin/callback/message

# 淘宝/千牛配置
TAOBAO_APP_KEY=your_taobao_appkey
TAOBAO_APP_SECRET=your_taobao_secret

# 小红书配置
XIAOHONGSHU_APP_ID=your_xhs_appid
XIAOHONGSHU_APP_SECRET=your_xhs_secret

# 1688配置
A1688_APP_KEY=your_1688_appkey
A1688_APP_SECRET=your_1688_secret

# AI配置（DeepSeek推荐）
OPENAI_API_KEY=sk-your-api-key
OPENAI_BASE_URL=https://api.deepseek.com

# JWT密钥
JWT_SECRET=your-secure-jwt-secret-key-change-this
EOF

# 加载环境变量
export $(cat .env | xargs)
```

### 3.3 启动后端服务

```bash
# 启动Java服务
cd java-services
mvn clean package -DskipTests

# 启动API网关
java -jar api-gateway/target/api-gateway.jar &

# 启动抖音平台服务
java -jar platform-douyin/target/platform-douyin.jar &

# 启动采购服务
java -jar purchase-service/target/purchase-service.jar &

# 启动Python客服服务
cd ../python-services/customer-service
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 &

# 启动千牛插件服务
cd ../qianniu-plugin
python -m app.main &
```

### 3.4 启动前端服务

```bash
cd frontend

# 生产构建
npm run build

# 使用Nginx部署
sudo cp -r dist/* /var/www/html/

# Nginx配置
sudo tee /etc/nginx/sites-available/ecommerce << 'EOF'
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name your-domain.com;
    
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    root /var/www/html;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF

sudo ln -s /etc/nginx/sites-available/ecommerce /etc/nginx/sites-enabled/
sudo nginx -t && sudo nginx -s reload
```

---

## 四、运营配置

### 4.1 配置店铺授权

登录管理界面后：

1. 进入「平台对接」页面
2. 点击对应平台的「连接平台」
3. 扫码或填写授权信息
4. 系统自动获取AccessToken

### 4.2 配置知识库

进入「知识库」页面，添加常见问答：

```json
[
  {
    "category": "商品咨询",
    "question": "这个商品有货吗？",
    "answer": "您好，该商品目前有现货，下单后24小时内发货。",
    "keywords": "有货,现货,库存"
  },
  {
    "category": "订单查询",
    "question": "我的订单什么时候发货？",
    "answer": "您好，订单付款后24小时内发货，您可以在订单详情查看物流信息。",
    "keywords": "发货,快递,物流"
  }
]
```

### 4.3 配置风控规则

系统默认风控规则：

| 规则 | 触发条件 | 处理方式 |
|------|----------|----------|
| 大额订单 | 金额>10000元 | 需人工确认采购 |
| 高频下单 | 同一买家100单/小时 | 触发风控审核 |
| 新买家 | 首次购买>5000元 | 需人工确认 |

可在数据库 `risk_rule` 表自定义规则。

### 4.4 配置1688预存款

混合支付需要1688账户预存资金：

1. 登录1688买家后台
2. 进入「账户中心」->「预存款充值」
3. 充值金额建议：日均采购额 × 7天

---

## 五、日常运营

### 5.1 监控告警

访问 Grafana：http://your-domain:3000

关键监控指标：
- 订单同步延迟（<5分钟正常）
- 客服消息响应时间（<3秒正常）
- 采购成功率（>95%正常）
- 服务CPU/内存使用率（<80%正常）

### 5.2 日志查看

```bash
# 查看应用日志
tail -f /var/log/ecommerce/app.log

# 查看错误日志
grep ERROR /var/log/ecommerce/app.log | tail -100

# Kibana访问
http://your-domain:5601
```

### 5.3 数据备份

```bash
# 每日数据库备份脚本
cat > /opt/scripts/backup.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d)
mysqldump -h 127.0.0.1 -P 4000 -u root ecommerce > /backup/ecommerce_$DATE.sql
# 保留30天
find /backup -name "ecommerce_*.sql" -mtime +30 -delete
EOF

chmod +x /opt/scripts/backup.sh

# 添加定时任务
echo "0 2 * * * /opt/scripts/backup.sh" | crontab -
```

---

## 六、常见问题处理

### 6.1 平台Token过期

```bash
# 重新授权
进入管理后台 -> 平台对接 -> 对应平台 -> 重新连接
```

### 6.2 订单同步失败

```bash
# 检查日志
grep "订单同步" /var/log/ecommerce/app.log | tail -50

# 手动触发同步
curl -X POST http://localhost:8081/api/v1/platform/douyin/sync
```

### 6.3 AI回复异常

```bash
# 检查AI服务状态
curl http://localhost:8000/api/v1/health

# 检查API余额
# 登录DeepSeek/OpenAI控制台查看
```

---

## 七、联系方式

- 技术支持：tech@yourcompany.com
- 运维值班：ops@yourcompany.com
- 紧急电话：xxx-xxxx-xxxx

---

## 附录：系统访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 管理后台 | https://your-domain.com | 主界面 |
| API文档 | https://your-domain.com/docs | Swagger |
| Grafana监控 | http://your-ip:3000 | 系统监控 |
| Kibana日志 | http://your-ip:5601 | 日志查询 |
| Nacos配置 | http://your-ip:8848/nacos | 配置中心 |