package com.ecommerce.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String TOKEN_PREFIX = "Bearer ";
    
    @Value("${auth.service.url:http://localhost:8083}")
    private String authServiceUrl;
    
    private static final List<String> WHITE_LIST = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/send-sms",
            "/api/v1/auth/health",
            "/actuator",
            "/health",
            "/api/v1/platform/*/callback",
            "/api/v1/platform/*/verify"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }
        
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (token == null || token.isEmpty()) {
            return unauthorized(exchange, "Missing authorization token");
        }
        
        if (!token.startsWith(TOKEN_PREFIX)) {
            return unauthorized(exchange, "Invalid token format");
        }
        
        return chain.filter(exchange);
    }
    
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> {
            if (pattern.contains("*")) {
                String regex = pattern.replace("*", ".*");
                return path.matches(regex);
            }
            return path.startsWith(pattern);
        });
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}