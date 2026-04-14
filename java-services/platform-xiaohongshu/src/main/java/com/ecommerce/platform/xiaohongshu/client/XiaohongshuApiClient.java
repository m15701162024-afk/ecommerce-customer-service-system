package com.ecommerce.platform.xiaohongshu.client;

import com.ecommerce.platform.xiaohongshu.config.XiaohongshuConfig;
import com.ecommerce.platform.xiaohongshu.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * 小红书API客户端
 * 
 * 实现功能:
 * - OAuth2.0授权认证
 * - 商品同步
 * - 订单同步
 * - 消息接收和发送
 * 
 * 高可用特性:
 * - 请求重试机制 (Resilience4j Retry)
 * - 熔断降级 (Resilience4j Circuit Breaker)
 * - 限流控制 (Resilience4j Rate Limiter)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class XiaohongshuApiClient {

    private final XiaohongshuConfig config;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== OAuth2.0 授权相关 ====================

    /**
     * 获取授权URL
     */
    public String getAuthorizationUrl(String state, String redirectUri) {
        return String.format("%s?app_id=%s&redirect_uri=%s&response_type=code&state=%s&scope=user_info,order_info,product_info,message",
                config.getAuthUrl(),
                config.getAppId(),
                redirectUri != null ? redirectUri : config.getCallbackUrl(),
                state);
    }

    /**
     * 使用授权码获取Access Token
     */
    @Retry(name = "xiaohongshuApi", fallbackMethod = "getAccessTokenFallback")
    @CircuitBreaker(name = "xiaohongshuApi", fallbackMethod = "getAccessTokenFallback")
    @RateLimiter(name = "xiaohongshuApi")
    public TokenResponse getAccessToken(String code) {
        log.info("获取小红书Access Token: code={}", code);

        try {
            Map<String, String> params = new HashMap<>();
            params.put("app_id", config.getAppId());
            params.put("app_secret", config.getAppSecret());
            params.put("grant_type", "authorization_code");
            params.put("code", code);

            TokenResponse response = webClient.post()
                    .uri(config.getTokenUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(params)
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();

            log.info("获取Access Token成功: code={}", response != null ? response.getCode() : null);
            return response;
        } catch (Exception e) {
            log.error("获取小红书Access Token失败", e);
            throw new RuntimeException("获取Access Token失败", e);
        }
    }

    /**
     * 刷新Access Token
     */
    @Retry(name = "xiaohongshuApi", fallbackMethod = "refreshTokenFallback")
    @CircuitBreaker(name = "xiaohongshuApi", fallbackMethod = "refreshTokenFallback")
    @RateLimiter(name = "xiaohongshuApi")
    public TokenResponse refreshToken(Long shopId, String refreshToken) {
        log.info("刷新小红书Access Token: shopId={}", shopId);

        try {
            Map<String, String> params = new HashMap<>();
            params.put("app_id", config.getAppId());
            params.put("app_secret", config.getAppSecret());
            params.put("grant_type", "refresh_token");
            params.put("refresh_token", refreshToken);

            TokenResponse response = webClient.post()
                    .uri(config.getTokenUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(params)
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();

            if (response != null && response.isSuccess()) {
                // 更新Token缓存
                TokenResponse.Data data = response.getData();
                config.setTokenInfo(shopId, data.getAccessToken(), data.getRefreshToken(), data.getExpiresIn());
            }

            log.info("刷新Access Token成功: shopId={}", shopId);
            return response;
        } catch (Exception e) {
            log.error("刷新小红书Access Token失败: shopId={}", shopId, e);
            throw new RuntimeException("刷新Access Token失败", e);
        }
    }

    // ==================== 订单相关 ====================

    /**
     * 获取订单列表
     */
    @Retry(name = "xiaohongshuApi", fallbackMethod = "getOrderListFallback")
    @CircuitBreaker(name = "xiaohongshuApi", fallbackMethod = "getOrderListFallback")
    @RateLimiter(name = "xiaohongshuApi")
    public OrderListResponse getOrderList(Long shopId, int page, int pageSize, Long startTime, Long endTime) {
        log.info("获取小红书订单列表: shopId={}, page={}, pageSize={}", shopId, page, pageSize);

        String accessToken = config.getAccessToken(shopId);
        if (accessToken == null) {
            log.error("店铺Token不存在或已过期: shopId={}", shopId);
            return null;
        }

        try {
            Map<String, Object> params = new TreeMap<>();
            params.put("app_id", config.getAppId());
            params.put("timestamp", Instant.now().getEpochSecond());
            params.put("page", page);
            params.put("page_size", pageSize);
            params.put("start_time", startTime);
            params.put("end_time", endTime);

            String sign = generateSignature(params);
            params.put("sign", sign);

            OrderListResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/sns/v1/order/list")
                            .queryParam("access_token", accessToken)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(params)
                    .retrieve()
                    .bodyToMono(OrderListResponse.class)
                    .block();

            log.info("获取订单列表成功: shopId={}, total={}", shopId, 
                    response != null && response.getData() != null ? response.getData().getTotal() : 0);
            return response;
        } catch (Exception e) {
            log.error("获取小红书订单列表失败: shopId={}", shopId, e);
            throw new RuntimeException("获取订单列表失败", e);
        }
    }

    /**
     * 获取订单详情
     */
    @Retry(name = "xiaohongshuApi", fallbackMethod = "getOrderDetailFallback")
    @CircuitBreaker(name = "xiaohongshuApi", fallbackMethod = "getOrderDetailFallback")
    @RateLimiter(name = "xiaohongshuApi")
    public OrderDetailResponse getOrderDetail(Long shopId, String orderId) {
        log.info("获取小红书订单详情: shopId={}, orderId={}", shopId, orderId);

        String accessToken = config.getAccessToken(shopId);
        if (accessToken == null) {
            log.error("店铺Token不存在或已过期: shopId={}", shopId);
            return null;
        }

        try {
            Map<String, Object> params = new TreeMap<>();
            params.put("app_id", config.getAppId());
            params.put("timestamp", Instant.now().getEpochSecond());
            params.put("order_id", orderId);

            String sign = generateSignature(params);
            params.put("sign", sign);

            OrderDetailResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/sns/v1/order/detail")
                            .queryParam("access_token", accessToken)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(params)
                    .retrieve()
                    .bodyToMono(OrderDetailResponse.class)
                    .block();

            log.info("获取订单详情成功: orderId={}", orderId);
            return response;
        } catch (Exception e) {
            log.error("获取小红书订单详情失败: orderId={}", orderId, e);
            throw new RuntimeException("获取订单详情失败", e);
        }
    }

    // ==================== 商品相关 ====================

    /**
     * 获取商品列表
     */
    @Retry(name = "xiaohongshuApi", fallbackMethod = "getProductListFallback")
    @CircuitBreaker(name = "xiaohongshuApi", fallbackMethod = "getProductListFallback")
    @RateLimiter(name = "xiaohongshuApi")
    public ProductListResponse getProductList(Long shopId, int page, int pageSize, Integer status) {
        log.info("获取小红书商品列表: shopId={}, page={}, pageSize={}", shopId, page, pageSize);

        String accessToken = config.getAccessToken(shopId);
        if (accessToken == null) {
            log.error("店铺Token不存在或已过期: shopId={}", shopId);
            return null;
        }

        try {
            Map<String, Object> params = new TreeMap<>();
            params.put("app_id", config.getAppId());
            params.put("timestamp", Instant.now().getEpochSecond());
            params.put("page", page);
            params.put("page_size", pageSize);
            if (status != null) {
                params.put("status", status);
            }

            String sign = generateSignature(params);
            params.put("sign", sign);

            ProductListResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/sns/v1/product/list")
                            .queryParam("access_token", accessToken)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(params)
                    .retrieve()
                    .bodyToMono(ProductListResponse.class)
                    .block();

            log.info("获取商品列表成功: shopId={}, total={}", shopId,
                    response != null && response.getData() != null ? response.getData().getTotal() : 0);
            return response;
        } catch (Exception e) {
            log.error("获取小红书商品列表失败: shopId={}", shopId, e);
            throw new RuntimeException("获取商品列表失败", e);
        }
    }

    // ==================== 消息相关 ====================

    /**
     * 发送消息
     */
    @Retry(name = "xiaohongshuApi", fallbackMethod = "sendMessageFallback")
    @CircuitBreaker(name = "xiaohongshuApi", fallbackMethod = "sendMessageFallback")
    @RateLimiter(name = "xiaohongshuApi")
    public SendMessageResponse sendMessage(Long shopId, SendMessageRequest request) {
        log.info("发送小红书消息: shopId={}, toUserId={}, msgType={}", 
                shopId, request.getToUserId(), request.getMsgType());

        String accessToken = config.getAccessToken(shopId);
        if (accessToken == null) {
            log.error("店铺Token不存在或已过期: shopId={}", shopId);
            return createErrorResponse("TOKEN_EXPIRED", "Token不存在或已过期");
        }

        try {
            Map<String, Object> params = new TreeMap<>();
            params.put("app_id", config.getAppId());
            params.put("timestamp", Instant.now().getEpochSecond());
            params.put("to_user_id", request.getToUserId());
            params.put("conversation_id", request.getConversationId());
            params.put("msg_type", request.getMsgType());
            params.put("content", request.getContent());

            String sign = generateSignature(params);
            params.put("sign", sign);

            SendMessageResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/sns/v1/message/send")
                            .queryParam("access_token", accessToken)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(params)
                    .retrieve()
                    .bodyToMono(SendMessageResponse.class)
                    .block();

            log.info("发送消息成功: toUserId={}, messageId={}", 
                    request.getToUserId(),
                    response != null && response.getData() != null ? response.getData().getMessageId() : null);
            return response;
        } catch (Exception e) {
            log.error("发送小红书消息失败: toUserId={}", request.getToUserId(), e);
            throw new RuntimeException("发送消息失败", e);
        }
    }

    /**
     * 发送文本消息
     */
    public SendMessageResponse sendTextMessage(Long shopId, String toUserId, String conversationId, String text) {
        SendMessageRequest request = new SendMessageRequest();
        request.setShopId(shopId);
        request.setToUserId(toUserId);
        request.setConversationId(conversationId);
        request.setMsgType("text");
        
        SendMessageRequest.TextContent content = new SendMessageRequest.TextContent();
        content.setText(text);
        request.setContent(content);
        
        return sendMessage(shopId, request);
    }

    // ==================== 签名相关 ====================

    /**
     * 生成API签名
     */
    private String generateSignature(Map<String, Object> params) {
        try {
            // 按key排序拼接
            StringBuilder sb = new StringBuilder();
            sb.append(config.getAppSecret());

            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);

            for (String key : keys) {
                Object value = params.get(key);
                if (value != null && !"sign".equals(key)) {
                    sb.append(key).append(value);
                }
            }

            sb.append(config.getAppSecret());

            // MD5加密
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            log.error("生成签名失败", e);
            return "";
        }
    }

    // ==================== 降级方法 ====================

    public TokenResponse getAccessTokenFallback(String code, Throwable t) {
        log.error("获取Access Token降级: code={}, error={}", code, t.getMessage());
        return createTokenErrorResponse("SERVICE_UNAVAILABLE", "服务暂时不可用，请稍后重试");
    }

    public TokenResponse refreshTokenFallback(Long shopId, String refreshToken, Throwable t) {
        log.error("刷新Token降级: shopId={}, error={}", shopId, t.getMessage());
        return createTokenErrorResponse("SERVICE_UNAVAILABLE", "服务暂时不可用，请稍后重试");
    }

    public OrderListResponse getOrderListFallback(Long shopId, int page, int pageSize, Long startTime, Long endTime, Throwable t) {
        log.error("获取订单列表降级: shopId={}, error={}", shopId, t.getMessage());
        return createOrderListErrorResponse("SERVICE_UNAVAILABLE", "服务暂时不可用，请稍后重试");
    }

    public OrderDetailResponse getOrderDetailFallback(Long shopId, String orderId, Throwable t) {
        log.error("获取订单详情降级: orderId={}, error={}", orderId, t.getMessage());
        return createOrderDetailErrorResponse("SERVICE_UNAVAILABLE", "服务暂时不可用，请稍后重试");
    }

    public ProductListResponse getProductListFallback(Long shopId, int page, int pageSize, Integer status, Throwable t) {
        log.error("获取商品列表降级: shopId={}, error={}", shopId, t.getMessage());
        return createProductListErrorResponse("SERVICE_UNAVAILABLE", "服务暂时不可用，请稍后重试");
    }

    public SendMessageResponse sendMessageFallback(Long shopId, SendMessageRequest request, Throwable t) {
        log.error("发送消息降级: toUserId={}, error={}", request.getToUserId(), t.getMessage());
        return createErrorResponse("SERVICE_UNAVAILABLE", "服务暂时不可用，请稍后重试");
    }

    // ==================== 辅助方法 ====================

    private TokenResponse createTokenErrorResponse(String code, String message) {
        TokenResponse response = new TokenResponse();
        response.setCode(-1);
        response.setMsg(message);
        return response;
    }

    private OrderListResponse createOrderListErrorResponse(String code, String message) {
        OrderListResponse response = new OrderListResponse();
        response.setCode(-1);
        response.setMsg(message);
        return response;
    }

    private OrderDetailResponse createOrderDetailErrorResponse(String code, String message) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setCode(-1);
        response.setMsg(message);
        return response;
    }

    private ProductListResponse createProductListErrorResponse(String code, String message) {
        ProductListResponse response = new ProductListResponse();
        response.setCode(-1);
        response.setMsg(message);
        return response;
    }

    private SendMessageResponse createErrorResponse(String code, String message) {
        SendMessageResponse response = new SendMessageResponse();
        response.setCode(-1);
        response.setMsg(message);
        return response;
    }
}