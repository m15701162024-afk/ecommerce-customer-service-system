package com.ecommerce.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/fallback")
@RequiredArgsConstructor
public class FallbackController {

    private final ObjectMapper objectMapper;

    @GetMapping("/auth")
    public Mono<Void> authFallback(ServerWebExchange exchange) {
        return createFallbackResponse(exchange, "认证服务暂时不可用，请稍后重试");
    }

    @GetMapping("/customer")
    public Mono<Void> customerFallback(ServerWebExchange exchange) {
        return createFallbackResponse(exchange, "客服服务暂时不可用，请稍后重试");
    }

    @GetMapping("/order")
    public Mono<Void> orderFallback(ServerWebExchange exchange) {
        return createFallbackResponse(exchange, "订单服务暂时不可用，请稍后重试");
    }

    @GetMapping("/purchase")
    public Mono<Void> purchaseFallback(ServerWebExchange exchange) {
        return createFallbackResponse(exchange, "采购服务暂时不可用，请稍后重试");
    }

    @GetMapping("/product")
    public Mono<Void> productFallback(ServerWebExchange exchange) {
        return createFallbackResponse(exchange, "商品服务暂时不可用，请稍后重试");
    }

    @GetMapping("/platform")
    public Mono<Void> platformFallback(ServerWebExchange exchange) {
        return createFallbackResponse(exchange, "平台对接服务暂时不可用，请稍后重试");
    }

    private Mono<Void> createFallbackResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 503);
        result.put("message", message);
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());

        try {
            String body = objectMapper.writeValueAsString(result);
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (Exception e) {
            log.error("Error creating fallback response", e);
            return Mono.error(e);
        }
    }
}