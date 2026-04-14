package com.ecommerce.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null 
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(ip);
        };
    }
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                return Mono.just(token.substring(7));
            }
            return Mono.just("anonymous");
        };
    }
}