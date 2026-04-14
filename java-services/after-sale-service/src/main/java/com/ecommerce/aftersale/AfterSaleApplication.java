package com.ecommerce.aftersale;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 售后服务启动类
 * 
 * 提供退款、退货、换货的全流程处理
 * 
 * @author ecommerce-team
 */
@SpringBootApplication(scanBasePackages = "com.ecommerce")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ecommerce")
@EnableTransactionManagement
@MapperScan("com.ecommerce.aftersale.mapper")
public class AfterSaleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AfterSaleApplication.class, args);
    }
}