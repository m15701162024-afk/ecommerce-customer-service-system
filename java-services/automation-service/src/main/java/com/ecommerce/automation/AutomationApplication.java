package com.ecommerce.automation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 自动化服务启动类
 * 负责自动回复、工作流引擎、触发器管理
 * 
 * @author ecommerce-team
 */
@SpringBootApplication(scanBasePackages = "com.ecommerce")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ecommerce")
@EnableTransactionManagement
@EnableScheduling
@MapperScan("com.ecommerce.automation.mapper")
public class AutomationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutomationApplication.class, args);
    }
}