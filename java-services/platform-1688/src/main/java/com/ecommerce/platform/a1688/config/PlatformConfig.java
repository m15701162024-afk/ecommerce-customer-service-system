package com.ecommerce.platform.a1688.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * 1688平台服务配置类
 * 
 * 配置WebClient、熔断器、重试机制等基础设施
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PlatformConfig {
    
    private final A1688Config a1688Config;
    
    /**
     * 配置WebClient用于API调用
     * 
     * @return WebClient实例
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }
    
    /**
     * Sentinel切面
     * 
     * @return SentinelResourceAspect
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
    
    /**
     * 熔断器注册表
     * 
     * @return CircuitBreakerRegistry
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofMillis(a1688Config.getCircuitBreaker().getWaitDurationInOpenState()))
                .permittedNumberOfCallsInHalfOpenState(a1688Config.getCircuitBreaker().getPermittedCallsInHalfOpenState())
                .slidingWindowSize(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .build();
        
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        
        // 注册事件监听器
        CircuitBreaker circuitBreaker = registry.circuitBreaker("a1688-api");
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.info("熔断器状态变更: {}", event))
                .onError(event -> log.error("熔断器记录错误: {}", event))
                .onSuccess(event -> log.debug("熔断器记录成功: {}", event));
        
        return registry;
    }
    
    /**
     * 重试注册表
     * 
     * @return RetryRegistry
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(a1688Config.getRetry().getMaxAttempts())
                .waitDuration(Duration.ofMillis(a1688Config.getRetry().getInitialInterval()))
                .retryOnException(throwable -> {
                    // 网络异常或服务端错误时重试
                    return throwable instanceof java.io.IOException 
                            || throwable instanceof java.util.concurrent.TimeoutException;
                })
                .build();
        
        RetryRegistry registry = RetryRegistry.of(config);
        
        // 注册事件监听器
        Retry retry = registry.retry("a1688-api");
        retry.getEventPublisher()
                .onRetry(event -> log.warn("API调用重试: 第{}次", event.getNumberOfRetryAttempts()))
                .onError(event -> log.error("API调用重试失败: {}", event.getLastThrowable().getMessage()))
                .onSuccess(event -> log.debug("API调用成功"));
        
        return registry;
    }
}