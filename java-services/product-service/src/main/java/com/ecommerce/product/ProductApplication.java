package com.ecommerce.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 商品服务启动类
 * 端口: 8086
 * 功能: 商品管理、货源匹配
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCaching
@MapperScan("com.ecommerce.product.mapper")
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
        System.out.println("==========================================");
        System.out.println("   商品服务 (Product Service) 启动成功!");
        System.out.println("   端口: 8086");
        System.out.println("==========================================");
    }
}