package com.ecommerce.platform.a1688;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 1688开放平台对接服务启动类
 * 
 * 提供以下核心功能：
 * - OAuth2.0授权认证
 * - 商品搜索和详情查询
 * - 采购订单创建和管理
 * - 订单状态同步
 * - 消息回调处理
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
public class A1688PlatformApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(A1688PlatformApplication.class, args);
    }
}