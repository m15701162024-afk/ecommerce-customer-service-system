package com.ecommerce.platform.xiaohongshu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 小红书开放平台对接服务启动类
 * 
 * 支持功能:
 * - OAuth2.0授权认证
 * - 商品同步
 * - 订单同步
 * - 消息接收和发送
 * - 回调验证
 * 
 * 高可用特性:
 * - 请求重试机制
 * - 熔断降级
 * - 限流控制
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
public class XiaohongshuPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiaohongshuPlatformApplication.class, args);
    }
}