-- ============================================
-- 电商客服系统 - 功能扩展数据库脚本
-- 版本: 2.0.0
-- ============================================

USE ecommerce;

-- ============================================
-- 一、工单系统
-- ============================================

-- 工单分类表
CREATE TABLE IF NOT EXISTS `work_order_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
    `code` VARCHAR(32) NOT NULL COMMENT '分类编码',
    `description` VARCHAR(256) COMMENT '描述',
    `sla_hours` INT DEFAULT 24 COMMENT 'SLA时长(小时)',
    `auto_assign_rule` JSON COMMENT '自动分配规则',
    `sort_order` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单分类表';

-- 工单主表
CREATE TABLE IF NOT EXISTS `work_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(64) NOT NULL COMMENT '工单编号',
    `session_id` VARCHAR(128) COMMENT '关联会话ID',
    `order_id` BIGINT COMMENT '关联订单ID',
    `buyer_id` VARCHAR(128) NOT NULL COMMENT '买家ID',
    `buyer_name` VARCHAR(128) COMMENT '买家名称',
    `shop_id` BIGINT COMMENT '店铺ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `priority` TINYINT DEFAULT 2 COMMENT '优先级:1紧急2一般3低',
    `status` VARCHAR(32) DEFAULT 'PENDING' COMMENT '状态:PENDING/PROCESSING/RESOLVED/CLOSED',
    `title` VARCHAR(256) NOT NULL COMMENT '工单标题',
    `content` TEXT COMMENT '工单内容',
    `attachments` JSON COMMENT '附件列表',
    `assignee_id` BIGINT COMMENT '处理人ID',
    `assignee_name` VARCHAR(64) COMMENT '处理人名称',
    `dept_id` BIGINT COMMENT '部门ID',
    `sla_deadline` DATETIME COMMENT 'SLA截止时间',
    `resolved_at` DATETIME COMMENT '解决时间',
    `closed_at` DATETIME COMMENT '关闭时间',
    `satisfaction` TINYINT COMMENT '满意度1-5',
    `feedback` VARCHAR(512) COMMENT '反馈内容',
    `created_by` BIGINT COMMENT '创建人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_status` (`status`),
    KEY `idx_assignee` (`assignee_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单主表';

-- 工单流转记录
CREATE TABLE IF NOT EXISTS `work_order_flow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `work_order_id` BIGINT NOT NULL,
    `from_status` VARCHAR(32) COMMENT '原状态',
    `to_status` VARCHAR(32) NOT NULL COMMENT '新状态',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_name` VARCHAR(64) COMMENT '操作人名称',
    `remark` VARCHAR(512) COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_work_order_id` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转记录';

-- ============================================
-- 二、消息模板系统
-- ============================================

-- 模板分类表
CREATE TABLE IF NOT EXISTS `template_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT COMMENT '父分类ID',
    `sort_order` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板分类表';

-- 消息模板表
CREATE TABLE IF NOT EXISTS `message_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `category_id` BIGINT COMMENT '分类ID',
    `content` TEXT NOT NULL COMMENT '模板内容',
    `variables` JSON COMMENT '变量定义: [{name, description, default}]',
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `creator_id` BIGINT COMMENT '创建人ID',
    `scope` VARCHAR(32) DEFAULT 'PERSONAL' COMMENT '范围:PERSONAL/TEAM/GLOBAL',
    `shop_id` BIGINT COMMENT '店铺ID',
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_creator` (`creator_id`),
    FULLTEXT KEY `ft_content` (`name`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';

-- ============================================
-- 三、售后处理系统
-- ============================================

-- 售后原因字典
CREATE TABLE IF NOT EXISTS `after_sale_reason` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `type` VARCHAR(32) NOT NULL COMMENT '类型:REFUND_ONLY/RETURN_REFUND/EXCHANGE',
    `reason_code` VARCHAR(32) NOT NULL COMMENT '原因编码',
    `reason_text` VARCHAR(256) NOT NULL COMMENT '原因文本',
    `need_evidence` TINYINT DEFAULT 0 COMMENT '是否需要凭证',
    `sort_order` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后原因字典';

-- 退货地址表
CREATE TABLE IF NOT EXISTS `return_address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `shop_id` BIGINT NOT NULL,
    `contact_name` VARCHAR(64) NOT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(32) NOT NULL COMMENT '联系电话',
    `province` VARCHAR(32) NOT NULL,
    `city` VARCHAR(32) NOT NULL,
    `district` VARCHAR(32) NOT NULL,
    `address` VARCHAR(256) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认',
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货地址表';

-- 售后单主表
CREATE TABLE IF NOT EXISTS `after_sale_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `after_sale_no` VARCHAR(64) NOT NULL COMMENT '售后单号',
    `order_id` BIGINT NOT NULL COMMENT '原订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '原订单号',
    `shop_id` BIGINT NOT NULL,
    `buyer_id` VARCHAR(128) NOT NULL,
    `buyer_name` VARCHAR(128),
    `type` VARCHAR(32) NOT NULL COMMENT '类型:REFUND_ONLY/RETURN_REFUND/EXCHANGE',
    `reason_code` VARCHAR(32) COMMENT '原因编码',
    `reason` VARCHAR(512) COMMENT '售后原因',
    `description` TEXT COMMENT '详细描述',
    `evidence_images` JSON COMMENT '凭证图片',
    `refund_amount` DECIMAL(12,2) COMMENT '退款金额',
    `status` VARCHAR(32) DEFAULT 'PENDING' COMMENT '状态',
    `approval_status` VARCHAR(32) COMMENT '审批状态',
    `approval_remark` VARCHAR(512) COMMENT '审批备注',
    `return_address_id` BIGINT COMMENT '退货地址ID',
    `return_logistics_company` VARCHAR(32) COMMENT '物流公司',
    `return_logistics_no` VARCHAR(64) COMMENT '物流单号',
    `return_time` DATETIME COMMENT '退货时间',
    `receive_time` DATETIME COMMENT '收货时间',
    `refund_time` DATETIME COMMENT '退款时间',
    `refund_channel` VARCHAR(32) COMMENT '退款渠道',
    `processor_id` BIGINT COMMENT '处理人ID',
    `processor_name` VARCHAR(64) COMMENT '处理人名称',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_after_sale_no` (`after_sale_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后单主表';

-- ============================================
-- 四、自动化流程引擎
-- ============================================

-- 自动回复规则表
CREATE TABLE IF NOT EXISTS `auto_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL COMMENT '规则名称',
    `type` VARCHAR(32) NOT NULL COMMENT '类型:KEYWORD/TIME/EVENT',
    `trigger_condition` JSON NOT NULL COMMENT '触发条件',
    `action_type` VARCHAR(32) NOT NULL COMMENT '动作类型:REPLY/TRANSFER/CREATE_TICKET',
    `action_config` JSON COMMENT '动作配置',
    `priority` INT DEFAULT 0 COMMENT '优先级(越大越优先)',
    `match_type` VARCHAR(32) DEFAULT 'CONTAIN' COMMENT '匹配方式:EXACT/CONTAIN/REGEX',
    `shop_id` BIGINT COMMENT '店铺ID(NULL为全局)',
    `status` TINYINT DEFAULT 1,
    `hit_count` INT DEFAULT 0 COMMENT '命中次数',
    `created_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自动回复规则表';

-- 工作流定义表
CREATE TABLE IF NOT EXISTS `workflow_definition` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL,
    `description` VARCHAR(512),
    `trigger_type` VARCHAR(32) NOT NULL COMMENT '触发类型',
    `trigger_config` JSON COMMENT '触发配置',
    `nodes` JSON COMMENT '工作流节点定义',
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流定义表';

-- 工作流实例表
CREATE TABLE IF NOT EXISTS `workflow_instance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `workflow_id` BIGINT NOT NULL,
    `business_type` VARCHAR(32) NOT NULL,
    `business_id` BIGINT NOT NULL,
    `current_node` VARCHAR(64) COMMENT '当前节点',
    `status` VARCHAR(32) DEFAULT 'RUNNING' COMMENT '状态:RUNNING/COMPLETED/FAILED',
    `context` JSON COMMENT '执行上下文',
    `error_message` TEXT COMMENT '错误信息',
    `started_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `completed_at` DATETIME,
    PRIMARY KEY (`id`),
    KEY `idx_workflow_id` (`workflow_id`),
    KEY `idx_business` (`business_type`, `business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流实例表';

-- ============================================
-- 五、CRM客户管理
-- ============================================

-- 客户档案表
CREATE TABLE IF NOT EXISTS `customer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `buyer_id` VARCHAR(128) NOT NULL COMMENT '买家ID',
    `platform` VARCHAR(32) NOT NULL COMMENT '平台',
    `shop_id` BIGINT COMMENT '店铺ID',
    `nickname` VARCHAR(128) COMMENT '昵称',
    `real_name` VARCHAR(64) COMMENT '真实姓名',
    `phone` VARCHAR(32) COMMENT '手机号',
    `email` VARCHAR(128) COMMENT '邮箱',
    `avatar` VARCHAR(512) COMMENT '头像',
    `gender` TINYINT COMMENT '性别:0未知1男2女',
    `birthday` DATE COMMENT '生日',
    `province` VARCHAR(32) COMMENT '省份',
    `city` VARCHAR(32) COMMENT '城市',
    `address` VARCHAR(512) COMMENT '地址',
    `total_orders` INT DEFAULT 0 COMMENT '总订单数',
    `total_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '总消费金额',
    `avg_order_amount` DECIMAL(12,2) COMMENT '平均客单价',
    `last_order_at` DATETIME COMMENT '最后下单时间',
    `last_chat_at` DATETIME COMMENT '最后咨询时间',
    `customer_level` VARCHAR(32) DEFAULT 'NORMAL' COMMENT '等级:NORMAL/SILVER/GOLD/PLATINUM/DIAMOND',
    `customer_value` DECIMAL(12,2) COMMENT '客户价值评分',
    `remark` VARCHAR(512) COMMENT '备注',
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_buyer` (`platform`, `buyer_id`, `shop_id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_level` (`customer_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户档案表';

-- 客户标签表
CREATE TABLE IF NOT EXISTS `customer_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(64) NOT NULL COMMENT '标签名称',
    `code` VARCHAR(32) NOT NULL COMMENT '标签编码',
    `color` VARCHAR(16) COMMENT '颜色',
    `icon` VARCHAR(64) COMMENT '图标',
    `description` VARCHAR(256) COMMENT '描述',
    `is_system` TINYINT DEFAULT 0 COMMENT '是否系统标签',
    `sort_order` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户标签表';

-- 客户标签关联表
CREATE TABLE IF NOT EXISTS `customer_tag_rel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `customer_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    `created_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_customer_tag` (`customer_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户标签关联表';

-- 客户分组表
CREATE TABLE IF NOT EXISTS `customer_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(64) NOT NULL,
    `description` VARCHAR(256),
    `filter_condition` JSON COMMENT '筛选条件',
    `customer_count` INT DEFAULT 0 COMMENT '客户数量',
    `is_dynamic` TINYINT DEFAULT 0 COMMENT '是否动态分组',
    `shop_id` BIGINT,
    `created_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户分组表';

-- 客户行为日志
CREATE TABLE IF NOT EXISTS `customer_behavior_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `customer_id` BIGINT NOT NULL,
    `behavior_type` VARCHAR(32) NOT NULL COMMENT '行为类型',
    `behavior_data` JSON COMMENT '行为数据',
    `session_id` VARCHAR(128),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_type` (`behavior_type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户行为日志';

-- ============================================
-- 六、质检监控
-- ============================================

-- 质检规则表
CREATE TABLE IF NOT EXISTS `quality_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL,
    `code` VARCHAR(32) NOT NULL,
    `type` VARCHAR(32) NOT NULL COMMENT '类型:SENSITIVE/RESPONSE/ATTITUDE/PROFESSIONAL',
    `rule_config` JSON NOT NULL COMMENT '规则配置',
    `score_weight` DECIMAL(5,2) DEFAULT 1.00 COMMENT '分值权重',
    `penalty_score` INT DEFAULT 0 COMMENT '扣分',
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检规则表';

-- 质检记录表
CREATE TABLE IF NOT EXISTS `quality_check_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_id` VARCHAR(128) NOT NULL,
    `message_id` VARCHAR(128),
    `agent_id` BIGINT COMMENT '客服ID',
    `rule_id` BIGINT,
    `score` DECIMAL(5,2) COMMENT '得分',
    `issue_type` VARCHAR(32) COMMENT '问题类型',
    `issue_detail` TEXT COMMENT '问题详情',
    `screenshot` VARCHAR(512) COMMENT '截图',
    `reviewer_id` BIGINT COMMENT '质检员ID',
    `status` VARCHAR(32) DEFAULT 'PENDING' COMMENT '状态:PENDING/CONFIRMED/APPEALED',
    `appeal_reason` VARCHAR(512) COMMENT '申诉理由',
    `appeal_result` VARCHAR(32) COMMENT '申诉结果',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_agent_id` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检记录表';

-- 敏感词表
CREATE TABLE IF NOT EXISTS `sensitive_word` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `word` VARCHAR(128) NOT NULL COMMENT '敏感词',
    `category` VARCHAR(32) NOT NULL COMMENT '分类:POLITICAL/PORNOGRAPHY/ADVERTISING/INSULT',
    `level` TINYINT DEFAULT 1 COMMENT '级别:1低2中3高',
    `action` VARCHAR(32) DEFAULT 'WARN' COMMENT '动作:WARN/BLOCK/REPLACE',
    `replace_word` VARCHAR(128) COMMENT '替换词',
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- ============================================
-- 七、会话评价
-- ============================================

-- 会话评价表
CREATE TABLE IF NOT EXISTS `session_rating` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_id` VARCHAR(128) NOT NULL,
    `buyer_id` VARCHAR(128) NOT NULL,
    `agent_id` BIGINT COMMENT '客服ID',
    `rating` TINYINT NOT NULL COMMENT '评分1-5',
    `tags` JSON COMMENT '评价标签',
    `comment` VARCHAR(512) COMMENT '评价内容',
    `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session` (`session_id`),
    KEY `idx_agent_id` (`agent_id`),
    KEY `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话评价表';

-- 评价标签表
CREATE TABLE IF NOT EXISTS `rating_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(64) NOT NULL,
    `type` VARCHAR(32) NOT NULL COMMENT '类型:POSITIVE/NEGATIVE',
    `sort_order` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价标签表';

-- ============================================
-- 八、客服排班绩效
-- ============================================

-- 客服信息表
CREATE TABLE IF NOT EXISTS `agent` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `employee_no` VARCHAR(32) COMMENT '工号',
    `department` VARCHAR(64) COMMENT '部门',
    `skills` JSON COMMENT '技能标签',
    `max_sessions` INT DEFAULT 5 COMMENT '最大并发会话数',
    `status` VARCHAR(32) DEFAULT 'OFFLINE' COMMENT '状态:ONLINE/Busy/OFFLINE',
    `last_online_at` DATETIME COMMENT '最后在线时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服信息表';

-- 排班计划表
CREATE TABLE IF NOT EXISTS `agent_schedule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `agent_id` BIGINT NOT NULL,
    `work_date` DATE NOT NULL COMMENT '工作日期',
    `start_time` TIME NOT NULL COMMENT '开始时间',
    `end_time` TIME NOT NULL COMMENT '结束时间',
    `shift_type` VARCHAR(32) DEFAULT 'DAY' COMMENT '班次:MORNING/AFTERNOON/NIGHT',
    `status` VARCHAR(32) DEFAULT 'SCHEDULED' COMMENT '状态',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agent_date` (`agent_id`, `work_date`),
    KEY `idx_work_date` (`work_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班计划表';

-- 客服绩效表
CREATE TABLE IF NOT EXISTS `agent_performance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `agent_id` BIGINT NOT NULL,
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `online_duration` INT DEFAULT 0 COMMENT '在线时长(分钟)',
    `total_sessions` INT DEFAULT 0 COMMENT '总会话数',
    `total_messages` INT DEFAULT 0 COMMENT '消息总数',
    `avg_response_time` INT COMMENT '平均响应时间(秒)',
    `first_response_time` INT COMMENT '首响时间(秒)',
    `resolution_rate` DECIMAL(5,2) COMMENT '解决率(%)',
    `satisfaction_rate` DECIMAL(5,2) COMMENT '满意度(%)',
    `transfer_rate` DECIMAL(5,2) COMMENT '转人工率(%)',
    `quality_score` DECIMAL(5,2) COMMENT '质检得分',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agent_date` (`agent_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服绩效表';

-- ============================================
-- 九、商品智能推荐
-- ============================================

-- 推荐规则表
CREATE TABLE IF NOT EXISTS `recommendation_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL,
    `trigger_intent` VARCHAR(32) NOT NULL COMMENT '触发意图',
    `trigger_keywords` JSON COMMENT '触发关键词',
    `product_ids` JSON COMMENT '推荐商品ID列表',
    `max_count` INT DEFAULT 3 COMMENT '最大推荐数量',
    `priority` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_intent` (`trigger_intent`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐规则表';

-- 推荐日志表
CREATE TABLE IF NOT EXISTS `recommendation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_id` VARCHAR(128) NOT NULL,
    `buyer_id` VARCHAR(128),
    `rule_id` BIGINT,
    `recommended_products` JSON COMMENT '推荐商品',
    `clicked_product_id` BIGINT COMMENT '点击商品ID',
    `ordered_product_id` BIGINT COMMENT '下单商品ID',
    `order_amount` DECIMAL(12,2) COMMENT '订单金额',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐日志表';

-- ============================================
-- 十、初始化数据
-- ============================================

-- 工单分类
INSERT INTO `work_order_category` (`name`, `code`, `description`, `sla_hours`) VALUES
('咨询类', 'CONSULT', '一般咨询问题', 24),
('投诉类', 'COMPLAINT', '客户投诉', 4),
('售后类', 'AFTER_SALE', '售后问题', 48),
('技术支持', 'TECH_SUPPORT', '技术问题', 8),
('建议反馈', 'FEEDBACK', '客户建议', 72);

-- 售后原因
INSERT INTO `after_sale_reason` (`type`, `reason_code`, `reason_text`, `need_evidence`) VALUES
('REFUND_ONLY', 'NOT_NEEDED', '不想要了/拍错了', 0),
('REFUND_ONLY', 'CHEAPER_ELSEWHERE', '其他平台更便宜', 0),
('RETURN_REFUND', 'QUALITY_ISSUE', '商品质量问题', 1),
('RETURN_REFUND', 'WRONG_ITEM', '发错货/漏发', 1),
('RETURN_REFUND', 'NOT_AS_DESCRIBED', '商品与描述不符', 1),
('RETURN_REFUND', 'DAMAGED', '商品破损', 1),
('EXCHANGE', 'SIZE_WRONG', '尺码不合适', 0),
('EXCHANGE', 'COLOR_WRONG', '颜色/款式不喜欢', 0);

-- 客户标签
INSERT INTO `customer_tag` (`name`, `code`, `color`, `description`, `is_system`) VALUES
('VIP客户', 'VIP', '#FFD700', '高价值客户', 1),
('新客户', 'NEW', '#4CAF50', '首次购买客户', 1),
('复购客户', 'REPEAT', '#2196F3', '多次购买客户', 1),
('活跃客户', 'ACTIVE', '#00BCD4', '近期活跃客户', 1),
('沉默客户', 'SILENT', '#9E9E9E', '长期未活跃', 1),
('高价值', 'HIGH_VALUE', '#E91E63', '消费金额高', 1),
('价格敏感', 'PRICE_SENSITIVE', '#FF9800', '关注优惠', 1),
('投诉风险', 'COMPLAINT_RISK', '#F44336', '有投诉记录', 1);

-- 评价标签
INSERT INTO `rating_tag` (`name`, `type`, `sort_order`) VALUES
('回复及时', 'POSITIVE', 1),
('态度好', 'POSITIVE', 2),
('专业耐心', 'POSITIVE', 3),
('问题解决', 'POSITIVE', 4),
('回复慢', 'NEGATIVE', 1),
('态度差', 'NEGATIVE', 2),
('未解决问题', 'NEGATIVE', 3),
('答非所问', 'NEGATIVE', 4);

-- 敏感词
INSERT INTO `sensitive_word` (`word`, `category`, `level`, `action`) VALUES
('微信', 'ADVERTISING', 1, 'WARN'),
('加我', 'ADVERTISING', 1, 'WARN'),
('好评返现', 'ADVERTISING', 2, 'BLOCK'),
('刷单', 'ADVERTISING', 2, 'BLOCK');

SELECT 'Extension database initialization completed!' AS result;