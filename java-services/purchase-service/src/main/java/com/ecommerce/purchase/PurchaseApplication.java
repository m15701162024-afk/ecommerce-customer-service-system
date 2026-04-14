package com.ecommerce.purchase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 采购服务启动类
 * 端口: 8085
 */
@SpringBootApplication
@EnableFeignClients
public class PurchaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(PurchaseApplication.class, args);
        System.out.println("====================================");
        System.out.println("   采购服务启动成功! 端口: 8085");
        System.out.println("====================================");
    }
}