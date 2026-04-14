package com.ecommerce.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * CRM Customer Management Service Application
 * 
 * Provides customer profile, tagging, grouping and behavior analysis.
 * Running on port 8086.
 * 
 * @author E-Commerce Team
 */
@SpringBootApplication
@EnableJpaAuditing
public class CrmApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CrmApplication.class, args);
    }
}