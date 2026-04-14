package com.ecommerce.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String START_TIME_ATTR = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }
        
        final String finalRequestId = requestId;
        
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .build();
        
        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());
        
        log.info("[{}] Request: {} {} from {}", 
                finalRequestId, 
                request.getMethod(), 
                request.getPath().value(),
                request.getRemoteAddress());
        
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    Long startTime = exchange.getAttribute(START_TIME_ATTR);
                    if (startTime != null) {
                        long duration = System.currentTimeMillis() - startTime;
                        int statusCode = exchange.getResponse().getStatusCode() != null 
                                ? exchange.getResponse().getStatusCode().value() 
                                : 0;
                        log.info("[{}] Response: {} - {}ms", 
                                finalRequestId, statusCode, duration);
                    }
                }));
    }

    @Override
    public int getOrder() {
        return -200;
    }
}