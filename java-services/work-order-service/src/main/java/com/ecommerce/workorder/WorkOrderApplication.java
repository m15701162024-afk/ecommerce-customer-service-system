package com.ecommerce.workorder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 工单服务启动类
 * 
 * 提供工单创建、分配、流转、解决、SLA时效管理等完整功能
 * 运行端口: 8088
 * 
 * @author E-Commerce Team
 */
@SpringBootApplication
@EnableFeignClients
@MapperScan("com.ecommerce.workorder.mapper")
public class WorkOrderApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(WorkOrderApplication.class, args);
    }
}