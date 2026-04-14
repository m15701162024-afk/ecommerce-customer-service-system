# 电商客服系统 v2.0 产品功能定义文档

> 版本: v2.0.0 | 更新日期: 2026-03-24

---

## 一、功能模块总览

```
┌─────────────────────────────────────────────────────────────────────┐
│                        电商客服系统 v2.0                              │
├─────────────────────────────────────────────────────────────────────┤
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐       │
│  │智能客服 │ │工单系统 │ │售后处理 │ │自动化引擎│ │CRM管理  │       │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘       │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐       │
│  │消息模板 │ │质检监控 │ │数据分析 │ │排班绩效 │ │商品推荐 │       │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘       │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐                                │
│  │订单管理 │ │采购管理 │ │平台对接 │                                │
│  └─────────┘ └─────────┘ └─────────┘                                │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、功能模块详细定义

### 2.1 智能客服模块

#### 功能概述
基于AI的智能客服系统，支持意图识别、知识库检索、自动回复。

#### 功能清单

| 功能ID | 功能名称 | 功能描述 | 优先级 | 状态 |
|--------|----------|----------|--------|------|
| CS-001 | 意图识别 | 自动识别用户咨询意图 | P0 | ✅ |
| CS-002 | 知识库管理 | FAQ问答库维护 | P0 | ✅ |
| CS-003 | 自动回复 | 基于意图自动回复 | P0 | ✅ |
| CS-004 | 多轮对话 | 支持上下文理解 | P1 | ✅ |
| CS-005 | 转人工 | 无法解决时转人工 | P0 | ✅ |
| CS-006 | 会话管理 | 会话状态管理 | P0 | ✅ |
| CS-007 | 敏感词过滤 | 自动过滤敏感词 | P1 | ✅ |
| CS-008 | 满意度评价 | 服务质量评价 | P1 | ✅ |

#### 接口定义

```
POST   /api/v1/chat/send           发送消息
GET    /api/v1/chat/sessions       获取会话列表
GET    /api/v1/chat/sessions/{id}  获取会话详情
POST   /api/v1/chat/sessions/{id}/transfer  转人工
POST   /api/v1/sessions/{id}/rating 提交评价
```

#### 数据模型

```sql
-- 客服会话表
chat_session (
  id, session_id, shop_id, platform, buyer_id,
  status, assigned_to, created_at
)

-- 客服消息表
chat_message (
  id, session_id, direction, message_type, content,
  sender_id, created_at
)

-- 知识库表
knowledge_base (
  id, category, question, answer, keywords,
  priority, hit_count, status
)
```

---

### 2.2 工单系统模块

#### 功能概述
工单创建、分配、流转、解决的完整生命周期管理。

#### 功能清单

| 功能ID | 功能名称 | 功能描述 | 优先级 | 状态 |
|--------|----------|----------|--------|------|
| WO-001 | 创建工单 | 创建问题工单 | P0 | ✅ |
| WO-002 | 工单分配 | 手动/自动分配 | P0 | ✅ |
| WO-003 | 工单转派 | 跨部门转派 | P0 | ✅ |
| WO-004 | 工单解决 | 填写解决方案 | P0 | ✅ |
| WO-005 | 工单关闭 | 满意度评价后关闭 | P0 | ✅ |
| WO-006 | SLA管理 | 时效管理与超时预警 | P0 | ✅ |
| WO-007 | 工单统计 | 多维度统计 | P1 | ✅ |
| WO-008 | 分类管理 | 工单分类配置 | P1 | ✅ |

#### 状态流转

```
PENDING(待处理)
    ↓ assign
ASSIGNED(已分配)
    ↓ start-processing
PROCESSING(处理中)
    ↓ resolve
RESOLVED(已解决)
    ↓ close
CLOSED(已关闭)
    ↑ reopen
REOPENED(已重开)
```

#### SLA配置

| 优先级 | 响应时效 | 解决时效 |
|--------|----------|----------|
| URGENT(紧急) | 1小时 | 4小时 |
| HIGH(高) | 2小时 | 8小时 |
| NORMAL(普通) | 4小时 | 24小时 |
| LOW(低) | 8小时 | 48小时 |

#### 接口定义

```
POST   /api/v1/work-orders              创建工单
GET    /api/v1/work-orders              工单列表
GET    /api/v1/work-orders/{id}         工单详情
POST   /api/v1/work-orders/{id}/assign  分配工单
POST   /api/v1/work-orders/{id}/transfer 转派工单
POST   /api/v1/work-orders/{id}/resolve 解决工单
POST   /api/v1/work-orders/{id}/close   关闭工单
POST   /api/v1/work-orders/{id}/reopen  重开工单
GET    /api/v1/work-orders/statistics   工单统计
GET    /api/v1/work-orders/overdue      超时工单
```

---

### 2.3 售后处理模块

#### 功能概述
处理买家退款、退货、换货请求的全流程管理。

#### 功能清单

| 功能ID | 功能名称 | 功能描述 | 优先级 | 状态 |
|--------|----------|----------|--------|------|
| AS-001 | 申请售后 | 提交售后申请 | P0 | ✅ |
| AS-002 | 审批售后 | 同意/拒绝 | P0 | ✅ |
| AS-003 | 退货物流 | 填写物流信息 | P0 | ✅ |
| AS-004 | 确认收货 | 商家确认退货 | P0 | ✅ |
| AS-005 | 执行退款 | 调用支付系统退款 | P0 | ✅ |
| AS-006 | 换货发货 | 换货商品发货 | P0 | ✅ |
| AS-007 | 售后统计 | 售后数据分析 | P1 | ✅ |
| AS-008 | 退货地址 | 退货地址管理 | P1 | ✅ |

#### 售后类型

| 类型 | 流程 |
|------|------|
| 仅退款 | 申请 → 审核 → 退款 |
| 退货退款 | 申请 → 审核 → 退货 → 退款 |
| 换货 | 申请 → 审核 → 退货 → 换货发货 |

#### 接口定义

```
POST   /api/v1/after-sales               申请售后
GET    /api/v1/after-sales               售后列表
GET    /api/v1/after-sales/{id}          售后详情
PUT    /api/v1/after-sales/{id}/approve  审批通过
PUT    /api/v1/after-sales/{id}/reject   审批拒绝
PUT    /api/v1/after-sales/{id}/logistics 填写物流
POST   /api/v1/after-sales/{id}/refund   执行退款
POST   /api/v1/after-sales/{id}/exchange-ship 换货发货
GET    /api/v1/after-sales/statistics    售后统计
GET    /api/v1/after-sales/reasons       售后原因
```

---

### 2.4 自动化引擎模块

#### 功能概述
基于规则和触发器的自动化客服工作流程。

#### 功能清单

| 功能ID | 功能名称 | 功能描述 | 优先级 | 状态 |
|--------|----------|----------|--------|------|
| AU-001 | 关键词规则 | 关键词自动回复 | P0 | ✅ |
| AU-002 | 事件规则 | 订单状态通知 | P0 | ✅ |
| AU-003 | 定时规则 | 定时任务触发 | P1 | ✅ |
| AU-004 | 规则管理 | 规则CRUD | P0 | ✅ |
| AU-005 | 执行日志 | 规则执行记录 | P1 | ✅ |
| AU-006 | 工作流引擎 | 多节点工作流 | P2 | ✅ |
| AU-007 | 欢迎语 | 首次咨询欢迎语 | P1 | ✅ |
| AU-008 | Webhook | 外部系统对接 | P2 | ✅ |

#### 规则类型

| 类型 | 触发方式 | 应用场景 |
|------|----------|----------|
| KEYWORD | 消息关键词匹配 | 自动回复 |
| EVENT | 系统事件触发 | 订单通知 |
| TIME | Cron定时触发 | 促销提醒 |

#### 接口定义

```
POST   /api/v1/automation/rules          创建规则
GET    /api/v1/automation/rules          规则列表
PUT    /api/v1/automation/rules/{id}     更新规则
DELETE /api/v1/automation/rules/{id}     删除规则
PUT    /api/v1/automation/rules/{id}/enable  启用规则
PUT    /api/v1/automation/rules/{id}/disable 禁用规则
POST   /api/v1/automation/rules/{id}/test    测试规则
POST   /api/v1/automation/workflows      创建工作流
POST   /api/v1/automation/workflows/{id}/start 启动工作流
```

---

### 2.5 CRM客户管理模块

#### 功能概述
客户档案、标签、分组、画像管理。

#### 功能清单

| 功能ID | 功能名称 | 功能描述 | 优先级 | 状态 |
|--------|----------|----------|--------|------|
| CR-001 | 客户档案 | 客户信息管理 | P0 | ✅ |
| CR-002 | 客户标签 | 标签管理 | P0 | ✅ |
| CR-003 | 客户分组 | 分组管理 | P1 | ✅ |
| CR-004 | 客户等级 | VIP等级管理 | P1 | ✅ |
| CR-005 | RFM评分 | 客户价值评分 | P1 | ✅ |
| CR-006 | 客户画像 | 消费偏好分析 | P2 | ✅ |
| CR-007 | 黑名单 | 黑名单管理 | P1 | ✅ |
| CR-008 | 客户统计 | 客户数据分析 | P1 | ✅ |

#### 客户等级

| 等级 | 条件 | 权益 |
|------|------|------|
| NORMAL | 注册用户 | 基础服务 |
| SILVER | 消费≥500 | 优先客服 |
| GOLD | 消费≥2000 | 专属客服 |
| PLATINUM | 消费≥5000 | 优先发货 |
| DIAMOND | 消费≥10000 | 全部权益 |

#### 接口定义

```
GET    /api/v1/customers                 客户列表
POST   /api/v1/customers                 创建客户
GET    /api/v1/customers/{id}            客户详情
PUT    /api/v1/customers/{id}            更新客户
DELETE /api/v1/customers/{id}            删除客户
POST   /api/v1/customers/{id}/tags       添加标签
DELETE /api/v1/customers/{id}/tags/{tagId} 移除标签
PUT    /api/v1/customers/{id}/group      设置分组
PUT    /api/v1/customers/{id}/level      更新等级
POST   /api/v1/customers/{id}/blacklist  加入黑名单
DELETE /api/v1/customers/{id}/blacklist  移出黑名单
GET    /api/v1/customers/statistics      客户统计
GET    /api/v1/customers/tags            标签列表
POST   /api/v1/customers/tags            创建标签
GET    /api/v1/customers/groups          分组列表
POST   /api/v1/customers/groups          创建分组
```

---

### 2.6 质检监控模块

#### 功能概述
客服质量检测与监控。

#### 功能清单

| 功能ID | 功能名称 | 功能描述 | 优先级 | 状态 |
|--------|----------|----------|--------|------|
| QC-001 | 质检规则 | 质检规则配置 | P0 | ✅ |
| QC-002 | 质检记录 | 质检结果记录 | P0 | ✅ |
| QC-003 | 敏感词检测 | 敏感词过滤 | P0 | ✅ |
| QC-004 | 敏感词管理 | 敏感词库管理 | P1 | ✅ |
| QC-005 | 质检评分 | 质检评分计算 | P1 | ✅ |
| QC-006 | 客服排行 | 客服绩效排名 | P1 | ✅ |
| QC-007 | 质检申诉 | 申诉处理 | P2 | ✅ |
| QC-008 | 质检报告 | 质检报告生成 | P2 | ✅ |

#### 质检规则类型

| 类型 | 检测内容 | 扣分 |
|------|----------|------|
| RESPONSE_SPEED | 响应时间 | >5分钟扣5分 |
| ATTITUDE | 态度检测 | 敏感词扣10分 |
| PROFESSIONAL | 专业度 | 匹配率<80%扣5分 |
| SATISFACTION | 满意度 | 差评扣20分 |

#### 接口定义

```
GET    /api/v1/quality/checks            质检记录列表
POST   /api/v1/quality/checks            创建质检记录
GET    /api/v1/quality/checks/{id}       质检详情
PUT    /api/v1/quality/checks/{id}/confirm 确认质检
POST   /api/v1/quality/checks/{id}/appeal 申诉
GET    /api/v1/quality/rules             质检规则列表
POST   /api/v1/quality/rules             创建规则
GET    /api/v1/quality/sensitive-words   敏感词列表
POST   /api/v1/quality/sensitive-words   添加敏感词
DELETE /api/v1/quality/sensitive-words/{id} 删除敏感词
POST   /api/v1/quality/sensitive-words/check 检测敏感词
```

---

### 2.7 数据分析模块

#### 功能概述
运营数据分析与可视化报表。

#### 功能清单

| 功能ID | 功能名称 | 功能描述 | 优先级 | 状态 |
|--------|----------|----------|--------|------|
| DA-001 | 运营概览 | 关键指标展示 | P0 | ✅ |
| DA-002 | 客服统计 | 客服绩效分析 | P0 | ✅ |
| DA-003 | 销售分析 | GMV/订单分析 | P0 | ✅ |
| DA-004 | 商品分析 | 商品销售分析 | P1 | ✅ |
| DA-005 | 趋势分析 | 数据趋势图表 | P1 | ✅ |
| DA-006 | 导出报表 | 数据导出 | P2 | ⏳ |

#### 关键指标

| 指标 | 说明 |
|------|------|
| 今日会话量 | 今日客服会话总数 |
| 今日订单量 | 今日订单数量 |
| 今日GMV | 今日成交总额 |
| 客服满意度 | 平均服务评分 |
| 平均响应时间 | 首次响应时间 |
| 解决率 | 问题解决比例 |

#### 接口定义

```
GET    /api/v1/analytics/overview        运营概览
GET    /api/v1/analytics/customer-service 客服统计
GET    /api/v1/analytics/sales           销售分析
GET    /api/v1/analytics/products        商品分析
GET    /api/v1/analytics/trend/{type}    趋势数据
```

---

### 2.8 会话评价模块

#### 功能概述
客服服务满意度评价。

#### 功能清单

| 功能ID | 功能名称 | 功能描述 | 优先级 | 状态 |
|--------|----------|----------|--------|------|
| EV-001 | 提交评价 | 满意度评分 | P0 | ✅ |
| EV-002 | 评价标签 | 评价标签选择 | P1 | ✅ |
| EV-003 | 评价统计 | 评价数据分析 | P1 | ✅ |
| EV-004 | 差评预警 | 低评分预警 | P1 | ✅ |

#### 评价标签

| 类型 | 标签 |
|------|------|
| 正面 | 回复及时、态度好、专业耐心、问题解决 |
| 负面 | 回复慢、态度差、未解决问题、答非所问 |

#### 接口定义

```
POST   /api/v1/sessions/{id}/rating      提交评价
GET    /api/v1/sessions/{id}/rating      评价详情
GET    /api/v1/ratings/statistics        评价统计
GET    /api/v1/ratings/tags              评价标签
```

---

### 2.9 客服排班绩效模块

#### 功能概述
客服人员排班与绩效管理。

#### 功能清单

| 功能ID | 功能名称 | 功能描述 | 优先级 | 状态 |
|--------|----------|----------|--------|------|
| SC-001 | 排班管理 | 排班计划管理 | P1 | ✅ |
| SC-002 | 在线状态 | 客服状态管理 | P0 | ✅ |
| SC-003 | 绩效统计 | 客服绩效计算 | P1 | ✅ |
| SC-004 | 排行榜 | 绩效排名 | P1 | ✅ |
| SC-005 | 考勤管理 | 打卡记录 | P2 | ✅ |

#### 绩效指标

| 指标 | 权重 | 说明 |
|------|------|------|
| 会话量 | 20% | 接待会话数量 |
| 响应时间 | 20% | 平均响应时长 |
| 满意度 | 30% | 服务评分 |
| 解决率 | 20% | 问题解决比例 |
| 质检分 | 10% | 质检评分 |

#### 接口定义

```
GET    /api/v1/schedules                 排班列表
POST   /api/v1/schedules                 创建排班
POST   /api/v1/schedules/batch           批量创建
PUT    /api/v1/schedules/{id}            更新排班
DELETE /api/v1/schedules/{id}            删除排班
GET    /api/v1/agents/status             客服状态
PUT    /api/v1/agents/status             更新状态
GET    /api/v1/agents/performance        绩效数据
GET    /api/v1/agents/ranking            客服排行
```

---

### 2.10 商品智能推荐模块

#### 功能概述
基于用户意图的智能商品推荐。

#### 功能清单

| 功能ID | 功能名称 | 功能描述 | 优先级 | 状态 |
|--------|----------|----------|--------|------|
| RC-001 | 意图推荐 | 根据意图推荐 | P1 | ✅ |
| RC-002 | 关联推荐 | 相关商品推荐 | P1 | ✅ |
| RC-003 | 热销推荐 | 热销商品推荐 | P1 | ✅ |
| RC-004 | 推荐规则 | 推荐规则配置 | P2 | ✅ |
| RC-005 | 效果追踪 | 推荐效果统计 | P2 | ✅ |

#### 接口定义

```
POST   /api/v1/recommend/products        商品推荐
GET    /api/v1/recommend/hot             热销推荐
GET    /api/v1/recommend/related/{id}    关联推荐
GET    /api/v1/recommend/rules           推荐规则
POST   /api/v1/recommend/rules           创建规则
```

---

## 三、平台对接服务

### 3.1 抖音平台

| 接口 | 说明 |
|------|------|
| POST /api/v1/platform/douyin/callback | 消息回调 |
| GET /api/v1/platform/douyin/callback/verify | 验证回调 |
| POST /api/v1/platform/douyin/message/send | 发送消息 |
| GET /api/v1/platform/douyin/orders | 订单同步 |

### 3.2 小红书平台

| 接口 | 说明 |
|------|------|
| POST /api/v1/platform/xiaohongshu/callback | 消息回调 |
| GET /api/v1/platform/xiaohongshu/callback/verify | 验证回调 |
| POST /api/v1/platform/xiaohongshu/message/send | 发送消息 |
| GET /api/v1/platform/xiaohongshu/products | 商品同步 |

### 3.3 1688平台

| 接口 | 说明 |
|------|------|
| POST /api/v1/platform/1688/callback | 回调处理 |
| GET /api/v1/platform/1688/callback/verify | 验证回调 |
| POST /api/v1/platform/1688/order/create | 创建采购单 |
| GET /api/v1/platform/1688/products | 商品搜索 |
| GET /api/v1/platform/1688/orders | 采购订单查询 |

---

## 四、错误码定义

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1001 | 用户名已存在 |
| 1002 | 密码错误 |
| 1003 | Token过期 |
| 2001 | 订单不存在 |
| 2002 | 订单状态异常 |
| 3001 | 工单不存在 |
| 3002 | 工单状态不允许操作 |
| 4001 | 售后单不存在 |
| 4002 | 售后状态不允许操作 |
| 5001 | 规则不存在 |
| 5002 | 规则配置无效 |

---

## 五、版本历史

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v2.0.0 | 2026-03-24 | 新增工单、售后、自动化、CRM等10大功能模块 |
| v1.0.0 | 2026-01-15 | 初始版本，基础客服和采购功能 |

---

**文档维护**: 产品功能变更时同步更新此文档。