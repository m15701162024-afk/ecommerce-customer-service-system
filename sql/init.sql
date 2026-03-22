-- ============================================
-- 全自动网店客服采购系统 - 数据库初始化脚本
-- 数据库: TiDB (兼容MySQL协议)
-- 版本: 1.0.0
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS ecommerce DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS nacos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ecommerce;

-- ============================================
-- 用户相关表
-- ============================================

-- 店铺表
CREATE TABLE IF NOT EXISTS `shop` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '店铺ID',
    `shop_name` VARCHAR(128) NOT NULL COMMENT '店铺名称',
    `platform` VARCHAR(32) NOT NULL COMMENT '平台: DOUYIN/TAOBAO/XIAOHONGSHU',
    `platform_shop_id` VARCHAR(64) NOT NULL COMMENT '平台店铺ID',
    `owner_id` BIGINT NOT NULL COMMENT '店主用户ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `access_token` TEXT COMMENT '平台访问令牌(加密)',
    `refresh_token` TEXT COMMENT '刷新令牌(加密)',
    `token_expire_time` DATETIME COMMENT '令牌过期时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform_shop` (`platform`, `platform_shop_id`),
    KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(256) NOT NULL COMMENT '密码(加密)',
    `real_name` VARCHAR(64) COMMENT '真实姓名',
    `phone` VARCHAR(32) COMMENT '手机号',
    `email` VARCHAR(128) COMMENT '邮箱',
    `avatar` VARCHAR(512) COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(64) COMMENT '最后登录IP',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(256) COMMENT '描述',
    `status` TINYINT NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================================
-- 商品相关表
-- ============================================

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `platform` VARCHAR(32) NOT NULL COMMENT '平台',
    `platform_product_id` VARCHAR(128) NOT NULL COMMENT '平台商品ID',
    `product_name` VARCHAR(512) NOT NULL COMMENT '商品名称',
    `product_image` VARCHAR(1024) COMMENT '主图URL',
    `category_id` BIGINT COMMENT '分类ID',
    `brand` VARCHAR(128) COMMENT '品牌',
    `price` DECIMAL(12, 2) NOT NULL COMMENT '售价',
    `original_price` DECIMAL(12, 2) COMMENT '原价',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    `source_product_id` VARCHAR(128) COMMENT '1688货源商品ID',
    `source_supplier_id` VARCHAR(128) COMMENT '1688供应商ID',
    `source_price` DECIMAL(12, 2) COMMENT '采购价',
    `profit_margin` DECIMAL(5, 2) COMMENT '利润率(%)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform_product` (`platform`, `platform_product_id`),
    KEY `idx_shop_id` (`shop_id`),
    KEY `idx_source_product` (`source_product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品SKU表
CREATE TABLE IF NOT EXISTS `product_sku` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL,
    `sku_code` VARCHAR(128) NOT NULL COMMENT 'SKU编码',
    `platform_sku_id` VARCHAR(128) COMMENT '平台SKU ID',
    `spec_values` JSON COMMENT '规格值JSON',
    `price` DECIMAL(12, 2) NOT NULL,
    `stock` INT NOT NULL DEFAULT 0,
    `source_sku_id` VARCHAR(128) COMMENT '1688货源SKU ID',
    `source_price` DECIMAL(12, 2) COMMENT '采购价',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_sku_code` (`sku_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

-- ============================================
-- 订单相关表
-- ============================================

-- 订单主表
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号',
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `platform` VARCHAR(32) NOT NULL COMMENT '平台',
    `platform_order_id` VARCHAR(128) NOT NULL COMMENT '平台订单ID',
    `buyer_id` VARCHAR(128) NOT NULL COMMENT '买家ID',
    `buyer_name` VARCHAR(128) COMMENT '买家名称',
    `buyer_phone` VARCHAR(32) COMMENT '买家手机',
    `buyer_address` VARCHAR(512) COMMENT '收货地址',
    `total_amount` DECIMAL(12, 2) NOT NULL COMMENT '订单总金额',
    `pay_amount` DECIMAL(12, 2) NOT NULL COMMENT '实付金额',
    `freight_amount` DECIMAL(12, 2) DEFAULT 0 COMMENT '运费',
    `discount_amount` DECIMAL(12, 2) DEFAULT 0 COMMENT '优惠金额',
    `status` VARCHAR(32) NOT NULL COMMENT '订单状态',
    `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态: 0-未支付 1-已支付',
    `ship_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发货状态: 0-未发货 1-已发货',
    `source_order_id` VARCHAR(128) COMMENT '1688采购订单ID',
    `source_status` VARCHAR(32) COMMENT '采购订单状态',
    `source_cost` DECIMAL(12, 2) COMMENT '采购成本',
    `profit` DECIMAL(12, 2) COMMENT '利润',
    `remark` VARCHAR(512) COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `paid_at` DATETIME COMMENT '支付时间',
    `shipped_at` DATETIME COMMENT '发货时间',
    `completed_at` DATETIME COMMENT '完成时间',
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    UNIQUE KEY `uk_platform_order` (`platform`, `platform_order_id`),
    KEY `idx_shop_id` (`shop_id`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 订单商品表
CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `sku_id` BIGINT,
    `product_name` VARCHAR(512) NOT NULL,
    `sku_name` VARCHAR(256),
    `product_image` VARCHAR(1024),
    `price` DECIMAL(12, 2) NOT NULL COMMENT '单价',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `total_amount` DECIMAL(12, 2) NOT NULL COMMENT '小计',
    `source_product_id` VARCHAR(128) COMMENT '1688货源商品ID',
    `source_sku_id` VARCHAR(128) COMMENT '1688货源SKU ID',
    `source_price` DECIMAL(12, 2) COMMENT '采购单价',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品表';

-- 订单状态变更日志
CREATE TABLE IF NOT EXISTS `order_status_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `from_status` VARCHAR(32) COMMENT '原状态',
    `to_status` VARCHAR(32) NOT NULL COMMENT '新状态',
    `operator` VARCHAR(64) COMMENT '操作人',
    `operator_type` VARCHAR(32) COMMENT '操作人类型: SYSTEM/ADMIN',
    `remark` VARCHAR(512) COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态变更日志';

-- ============================================
-- 采购相关表
-- ============================================

-- 供应商表
CREATE TABLE IF NOT EXISTS `supplier` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `supplier_id` VARCHAR(128) NOT NULL COMMENT '1688供应商ID',
    `supplier_name` VARCHAR(256) NOT NULL,
    `contact_name` VARCHAR(64) COMMENT '联系人',
    `contact_phone` VARCHAR(32) COMMENT '联系电话',
    `address` VARCHAR(512) COMMENT '地址',
    `credit_level` INT COMMENT '信用等级',
    `cooperation_status` TINYINT NOT NULL DEFAULT 1 COMMENT '合作状态: 0-停止 1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 采购订单表
CREATE TABLE IF NOT EXISTS `purchase_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `purchase_no` VARCHAR(64) NOT NULL COMMENT '采购单号',
    `order_id` BIGINT COMMENT '关联销售订单ID',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `total_amount` DECIMAL(12, 2) NOT NULL COMMENT '采购总金额',
    `status` VARCHAR(32) NOT NULL COMMENT '状态',
    `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态',
    `pay_type` VARCHAR(32) COMMENT '支付方式: AUTO/MANUAL',
    `need_manual_confirm` TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要人工确认',
    `confirm_user` VARCHAR(64) COMMENT '确认人',
    `confirm_time` DATETIME COMMENT '确认时间',
    `source_order_id` VARCHAR(128) COMMENT '1688订单ID',
    `remark` VARCHAR(512),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_no` (`purchase_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

-- ============================================
-- 客服相关表
-- ============================================

-- 客服会话表
CREATE TABLE IF NOT EXISTS `chat_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_id` VARCHAR(128) NOT NULL COMMENT '会话ID',
    `shop_id` BIGINT NOT NULL,
    `platform` VARCHAR(32) NOT NULL,
    `buyer_id` VARCHAR(128) NOT NULL COMMENT '买家ID',
    `buyer_name` VARCHAR(128),
    `buyer_avatar` VARCHAR(512),
    `last_message` TEXT COMMENT '最后一条消息',
    `last_message_time` DATETIME COMMENT '最后消息时间',
    `unread_count` INT NOT NULL DEFAULT 0 COMMENT '未读消息数',
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/CLOSED',
    `assigned_to` VARCHAR(64) COMMENT '分配给(AI/人工客服ID)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_id` (`session_id`),
    KEY `idx_shop_buyer` (`shop_id`, `buyer_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服会话表';

-- 客服消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_id` VARCHAR(128) NOT NULL,
    `message_id` VARCHAR(128) COMMENT '平台消息ID',
    `direction` TINYINT NOT NULL COMMENT '方向: 1-买家发送 2-系统发送',
    `message_type` VARCHAR(32) NOT NULL COMMENT '消息类型: TEXT/IMAGE/CARD',
    `content` TEXT COMMENT '消息内容',
    `media_url` VARCHAR(1024) COMMENT '媒体URL',
    `sender_id` VARCHAR(128) COMMENT '发送者ID',
    `sender_name` VARCHAR(128),
    `is_read` TINYINT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服消息表';

-- AI意图记录表
CREATE TABLE IF NOT EXISTS `ai_intent_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_id` VARCHAR(128) NOT NULL,
    `message_id` VARCHAR(128),
    `user_message` TEXT COMMENT '用户消息',
    `detected_intent` VARCHAR(64) COMMENT '识别意图',
    `confidence` DECIMAL(5, 4) COMMENT '置信度',
    `response_type` VARCHAR(32) COMMENT '响应类型',
    `response_content` TEXT COMMENT '响应内容',
    `handled_by` VARCHAR(32) COMMENT '处理者: AI/MANUAL',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI意图记录表';

-- 客服知识库表
CREATE TABLE IF NOT EXISTS `knowledge_base` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `shop_id` BIGINT COMMENT '店铺ID(NULL表示通用)',
    `category` VARCHAR(64) NOT NULL COMMENT '分类',
    `question` VARCHAR(512) NOT NULL COMMENT '问题',
    `answer` TEXT NOT NULL COMMENT '答案',
    `keywords` VARCHAR(512) COMMENT '关键词',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级',
    `hit_count` INT NOT NULL DEFAULT 0 COMMENT '命中次数',
    `status` TINYINT NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_shop_category` (`shop_id`, `category`),
    FULLTEXT KEY `ft_question` (`question`, `keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服知识库表';

-- ============================================
-- 支付与风控相关表
-- ============================================

-- 支付流水表
CREATE TABLE IF NOT EXISTS `payment_transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `transaction_no` VARCHAR(64) NOT NULL COMMENT '流水号',
    `order_id` BIGINT COMMENT '关联订单ID',
    `purchase_order_id` BIGINT COMMENT '关联采购订单ID',
    `transaction_type` VARCHAR(32) NOT NULL COMMENT '类型: INCOME/EXPENSE',
    `amount` DECIMAL(12, 2) NOT NULL COMMENT '金额',
    `channel` VARCHAR(32) COMMENT '渠道: ALIPAY/WECHAT/BALANCE',
    `channel_transaction_id` VARCHAR(128) COMMENT '渠道流水号',
    `status` VARCHAR(32) NOT NULL COMMENT '状态',
    `remark` VARCHAR(512),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_no` (`transaction_no`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水表';

-- 风控规则表
CREATE TABLE IF NOT EXISTS `risk_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `rule_code` VARCHAR(64) NOT NULL,
    `rule_name` VARCHAR(128) NOT NULL,
    `rule_type` VARCHAR(32) NOT NULL COMMENT '类型: ORDER/PAYMENT/BEHAVIOR',
    `rule_config` JSON COMMENT '规则配置',
    `action` VARCHAR(32) NOT NULL COMMENT '动作: BLOCK/REVIEW/PASS',
    `priority` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控规则表';

-- 风控记录表
CREATE TABLE IF NOT EXISTS `risk_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `business_type` VARCHAR(32) NOT NULL,
    `business_id` BIGINT NOT NULL,
    `rule_id` BIGINT,
    `risk_level` VARCHAR(32) NOT NULL COMMENT '风险等级: LOW/MEDIUM/HIGH',
    `risk_score` INT COMMENT '风险分数',
    `hit_rules` JSON COMMENT '命中规则',
    `action_taken` VARCHAR(32) COMMENT '执行动作',
    `operator` VARCHAR(64) COMMENT '处理人',
    `handle_result` VARCHAR(512) COMMENT '处理结果',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_business` (`business_type`, `business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控记录表';

-- ============================================
-- 系统配置表
-- ============================================

-- 平台配置表
CREATE TABLE IF NOT EXISTS `platform_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `platform` VARCHAR(32) NOT NULL,
    `config_key` VARCHAR(128) NOT NULL,
    `config_value` TEXT,
    `description` VARCHAR(512),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform_key` (`platform`, `config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台配置表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT,
    `username` VARCHAR(64),
    `operation` VARCHAR(128) NOT NULL,
    `method` VARCHAR(512),
    `params` TEXT,
    `ip` VARCHAR(64),
    `result` TEXT,
    `status` TINYINT,
    `error_msg` TEXT,
    `duration` BIGINT COMMENT '执行时长(ms)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- 初始化数据
-- ============================================

-- 初始化角色
INSERT INTO `role` (`role_name`, `role_code`, `description`) VALUES
('超级管理员', 'SUPER_ADMIN', '系统最高权限'),
('管理员', 'ADMIN', '系统管理员'),
('运营人员', 'OPERATOR', '店铺运营'),
('客服人员', 'CUSTOMER_SERVICE', '客服'),
('财务人员', 'FINANCE', '财务');

-- 初始化风控规则
INSERT INTO `risk_rule` (`rule_code`, `rule_name`, `rule_type`, `rule_config`, `action`, `priority`) VALUES
('ORDER_AMOUNT_LIMIT', '订单金额限制', 'ORDER', '{"maxAmount": 50000}', 'REVIEW', 100),
('ORDER_FREQUENCY_LIMIT', '订单频率限制', 'ORDER', '{"maxCount": 100, "timeWindow": 3600}', 'BLOCK', 90),
('NEW_BUYER_LARGE_ORDER', '新买家大额订单', 'ORDER', '{"orderCount": 5, "maxAmount": 10000}', 'REVIEW', 80);

-- 初始化平台配置
INSERT INTO `platform_config` (`platform`, `config_key`, `config_value`, `description`) VALUES
('DOUYIN', 'api_base_url', 'https://developer.toutiao.com', '抖音API基础URL'),
('DOUYIN', 'message_webhook', '', '消息推送回调地址'),
('TAOBAO', 'api_base_url', 'https://eco.taobao.com/router/rest', '淘宝API基础URL'),
('XIAOHONGSHU', 'api_base_url', 'https://open.xiaohongshu.com', '小红书API基础URL'),
('A1688', 'api_base_url', 'https://gw.open.1688.com/openapi', '1688 API基础URL'),
('SYSTEM', 'purchase_auto_threshold', '1000', '自动采购金额阈值(元)'),
('SYSTEM', 'purchase_need_manual_limit', '10000', '需要人工确认金额阈值(元)');

SELECT 'Database initialization completed!' AS result;