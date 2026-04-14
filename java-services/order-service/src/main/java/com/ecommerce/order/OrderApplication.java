package com.ecommerce.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 订单服务启动类
 * 
 * @author ecommerce-team
 */
@SpringBootApplication(scanBasePackages = "com.ecommerce")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ecommerce")
@EnableTransactionManagement
@MapperScan("com.ecommerce.order.mapper")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}