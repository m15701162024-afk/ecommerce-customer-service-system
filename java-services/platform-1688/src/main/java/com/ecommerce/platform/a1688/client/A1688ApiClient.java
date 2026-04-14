package com.ecommerce.platform.a1688.client;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.ecommerce.platform.a1688.config.A1688Config;
import com.ecommerce.platform.a1688.dto.request.*;
import com.ecommerce.platform.a1688.dto.response.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * 1688开放平台API客户端
 * 
 * 功能：
 * - OAuth2.0授权认证
 * - 签名生成
 * - 商品搜索和详情
 * - 采购订单管理
 * - 消息推送
 * 
 * 高可用特性：
 * - 自动重试
 * - 熔断降级
 * - 限流保护
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class A1688ApiClient {
    
    private final A1688Config config;
    private final ObjectMapper objectMapper;
    
    private static final String API_VERSION = "1.0";
    private static final String SIGN_METHOD = "sha256";
    private static final String FORMAT = "json";
    
    // ==================== OAuth2.0 授权相关 ====================
    
    /**
     * 生成OAuth2.0授权URL
     * 
     * @param state 自定义状态参数，用于防CSRF攻击
     * @param redirectUri 回调地址（可选，默认使用配置中的地址）
     * @return 授权URL
     */
    public String generateAuthUrl(String state, String redirectUri) {
        String callback = redirectUri != null ? redirectUri : config.getCallbackUrl();
        
        try {
            String encodedCallback = URLEncoder.encode(callback, StandardCharsets.UTF_8);
            return String.format("%s?client_id=%s&redirect_uri=%s&response_type=code&state=%s",
                    config.getAuthUrl(),
                    config.getAppKey(),
                    encodedCallback,
                    state);
        } catch (Exception e) {
            log.error("生成授权URL失败", e);
            throw new RuntimeException("生成授权URL失败", e);
        }
    }
    
    /**
     * 使用授权码换取Access Token
     * 
     * @param code 授权码
     * @return Token响应
     */
    @SentinelResource(value = "getAccessToken", blockHandler = "getAccessTokenBlockHandler", fallback = "getAccessTokenFallback")
    @CircuitBreaker(name = "a1688-api", fallbackMethod = "getAccessTokenFallback")
    @Retry(name = "a1688-api")
    public TokenResponse getAccessToken(String code) {
        log.info("使用授权码换取Token: code={}", code);
        
        try {
            Map<String, String> params = new TreeMap<>();
            params.put("client_id", config.getAppKey());
            params.put("client_secret", config.getAppSecret());
            params.put("grant_type", "authorization_code");
            params.put("code", code);
            params.put("redirect_uri", config.getCallbackUrl());
            
            String response = executePost(config.getTokenUrl(), params);
            
            TokenResponse tokenResponse = objectMapper.readValue(response, TokenResponse.class);
            log.info("Token获取成功: memberId={}", tokenResponse.getMemberId());
            
            return tokenResponse;
        } catch (Exception e) {
            log.error("获取Access Token失败", e);
            throw new RuntimeException("获取Access Token失败", e);
        }
    }
    
    /**
     * 刷新Access Token
     * 
     * @param refreshToken 刷新令牌
     * @return Token响应
     */
    @SentinelResource(value = "refreshAccessToken", blockHandler = "refreshTokenBlockHandler")
    @CircuitBreaker(name = "a1688-api", fallbackMethod = "refreshTokenFallback")
    @Retry(name = "a1688-api")
    public TokenResponse refreshAccessToken(String refreshToken) {
        log.info("刷新Access Token");
        
        try {
            Map<String, String> params = new TreeMap<>();
            params.put("client_id", config.getAppKey());
            params.put("client_secret", config.getAppSecret());
            params.put("grant_type", "refresh_token");
            params.put("refresh_token", refreshToken);
            
            String response = executePost(config.getTokenUrl(), params);
            
            TokenResponse tokenResponse = objectMapper.readValue(response, TokenResponse.class);
            log.info("Token刷新成功: memberId={}", tokenResponse.getMemberId());
            
            return tokenResponse;
        } catch (Exception e) {
            log.error("刷新Access Token失败", e);
            throw new RuntimeException("刷新Access Token失败", e);
        }
    }
    
    // ==================== 商品相关 ====================
    
    /**
     * 商品搜索
     * 
     * @param shopId 店铺ID
     * @param request 搜索请求
     * @return 搜索结果
     */
    @SentinelResource(value = "searchProducts", blockHandler = "searchProductsBlockHandler")
    @CircuitBreaker(name = "a1688-api", fallbackMethod = "searchProductsFallback")
    @Retry(name = "a1688-api")
    public ProductSearchResponse searchProducts(Long shopId, ProductSearchRequest request) {
        log.info("搜索商品: shopId={}, keyword={}", shopId, request.getKeyword());
        
        try {
            Map<String, Object> apiParams = buildApiParams(shopId, "alibaba.icbu.product.search", request);
            
            String response = executeApiCall(apiParams);
            
            ProductSearchResponse searchResponse = objectMapper.readValue(response, 
                    new TypeReference<ProductSearchResponse>() {});
            log.info("商品搜索完成: total={}", searchResponse.getTotalCount());
            
            return searchResponse;
        } catch (Exception e) {
            log.error("商品搜索失败", e);
            throw new RuntimeException("商品搜索失败", e);
        }
    }
    
    /**
     * 获取商品详情
     * 
     * @param shopId 店铺ID
     * @param productId 商品ID
     * @return 商品详情
     */
    @SentinelResource(value = "getProductDetail", blockHandler = "getProductDetailBlockHandler")
    @CircuitBreaker(name = "a1688-api", fallbackMethod = "getProductDetailFallback")
    @Retry(name = "a1688-api")
    public ProductDetailResponse getProductDetail(Long shopId, String productId) {
        log.info("获取商品详情: shopId={}, productId={}", shopId, productId);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("productId", productId);
            
            Map<String, Object> apiParams = buildApiParams(shopId, "alibaba.icbu.product.get", params);
            
            String response = executeApiCall(apiParams);
            
            ProductDetailResponse detailResponse = objectMapper.readValue(response,
                    new TypeReference<ProductDetailResponse>() {});
            log.info("获取商品详情成功: productId={}", productId);
            
            return detailResponse;
        } catch (Exception e) {
            log.error("获取商品详情失败: productId={}", productId, e);
            throw new RuntimeException("获取商品详情失败", e);
        }
    }
    
    // ==================== 采购订单相关 ====================
    
    /**
     * 创建采购订单
     * 
     * @param shopId 店铺ID
     * @param request 订单请求
     * @return 订单响应
     */
    @SentinelResource(value = "createPurchaseOrder", blockHandler = "createPurchaseOrderBlockHandler")
    @CircuitBreaker(name = "a1688-api", fallbackMethod = "createPurchaseOrderFallback")
    @Retry(name = "a1688-api")
    public PurchaseOrderResponse createPurchaseOrder(Long shopId, PurchaseOrderRequest request) {
        log.info("创建采购订单: shopId={}, productId={}", shopId, request.getProductId());
        
        try {
            Map<String, Object> apiParams = buildApiParams(shopId, "alibaba.icbu.trade.order.create", request);
            
            String response = executeApiCall(apiParams);
            
            PurchaseOrderResponse orderResponse = objectMapper.readValue(response,
                    new TypeReference<PurchaseOrderResponse>() {});
            log.info("采购订单创建成功: orderId={}", orderResponse.getOrderId());
            
            return orderResponse;
        } catch (Exception e) {
            log.error("创建采购订单失败", e);
            throw new RuntimeException("创建采购订单失败", e);
        }
    }
    
    /**
     * 获取订单详情
     * 
     * @param shopId 店铺ID
     * @param orderId 订单ID
     * @return 订单详情
     */
    @SentinelResource(value = "getOrderDetail", blockHandler = "getOrderDetailBlockHandler")
    @CircuitBreaker(name = "a1688-api", fallbackMethod = "getOrderDetailFallback")
    @Retry(name = "a1688-api")
    public OrderDetailResponse getOrderDetail(Long shopId, String orderId) {
        log.info("获取订单详情: shopId={}, orderId={}", shopId, orderId);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("orderId", orderId);
            
            Map<String, Object> apiParams = buildApiParams(shopId, "alibaba.icbu.trade.order.get", params);
            
            String response = executeApiCall(apiParams);
            
            OrderDetailResponse detailResponse = objectMapper.readValue(response,
                    new TypeReference<OrderDetailResponse>() {});
            log.info("获取订单详情成功: orderId={}, status={}", orderId, detailResponse.getStatus());
            
            return detailResponse;
        } catch (Exception e) {
            log.error("获取订单详情失败: orderId={}", orderId, e);
            throw new RuntimeException("获取订单详情失败", e);
        }
    }
    
    /**
     * 查询订单列表
     * 
     * @param shopId 店铺ID
     * @param request 订单查询请求
     * @return 订单列表响应
     */
    @SentinelResource(value = "queryOrderList", blockHandler = "queryOrderListBlockHandler")
    @CircuitBreaker(name = "a1688-api", fallbackMethod = "queryOrderListFallback")
    @Retry(name = "a1688-api")
    public OrderListResponse queryOrderList(Long shopId, OrderQueryRequest request) {
        log.info("查询订单列表: shopId={}, page={}", shopId, request.getPageNo());
        
        try {
            Map<String, Object> apiParams = buildApiParams(shopId, "alibaba.icbu.trade.order.list", request);
            
            String response = executeApiCall(apiParams);
            
            OrderListResponse listResponse = objectMapper.readValue(response,
                    new TypeReference<OrderListResponse>() {});
            log.info("查询订单列表成功: total={}", listResponse.getTotalCount());
            
            return listResponse;
        } catch (Exception e) {
            log.error("查询订单列表失败", e);
            throw new RuntimeException("查询订单列表失败", e);
        }
    }
    
    /**
     * 取消订单
     * 
     * @param shopId 店铺ID
     * @param orderId 订单ID
     * @param reason 取消原因
     * @return 操作结果
     */
    @SentinelResource(value = "cancelOrder", blockHandler = "cancelOrderBlockHandler")
    @CircuitBreaker(name = "a1688-api", fallbackMethod = "cancelOrderFallback")
    @Retry(name = "a1688-api")
    public BaseResponse cancelOrder(Long shopId, String orderId, String reason) {
        log.info("取消订单: shopId={}, orderId={}, reason={}", shopId, orderId, reason);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("orderId", orderId);
            params.put("cancelReason", reason);
            
            Map<String, Object> apiParams = buildApiParams(shopId, "alibaba.icbu.trade.order.cancel", params);
            
            String response = executeApiCall(apiParams);
            
            BaseResponse baseResponse = objectMapper.readValue(response, BaseResponse.class);
            log.info("取消订单成功: orderId={}", orderId);
            
            return baseResponse;
        } catch (Exception e) {
            log.error("取消订单失败: orderId={}", orderId, e);
            throw new RuntimeException("取消订单失败", e);
        }
    }
    
    /**
     * 确认收货
     * 
     * @param shopId 店铺ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    @SentinelResource(value = "confirmReceive", blockHandler = "confirmReceiveBlockHandler")
    @CircuitBreaker(name = "a1688-api", fallbackMethod = "confirmReceiveFallback")
    @Retry(name = "a1688-api")
    public BaseResponse confirmReceive(Long shopId, String orderId) {
        log.info("确认收货: shopId={}, orderId={}", shopId, orderId);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("orderId", orderId);
            
            Map<String, Object> apiParams = buildApiParams(shopId, "alibaba.icbu.trade.order.confirm", params);
            
            String response = executeApiCall(apiParams);
            
            BaseResponse baseResponse = objectMapper.readValue(response, BaseResponse.class);
            log.info("确认收货成功: orderId={}", orderId);
            
            return baseResponse;
        } catch (Exception e) {
            log.error("确认收货失败: orderId={}", orderId, e);
            throw new RuntimeException("确认收货失败", e);
        }
    }
    
    // ==================== 消息推送相关 ====================
    
    /**
     * 发送消息给供应商
     * 
     * @param shopId 店铺ID
     * @param request 消息请求
     * @return 发送结果
     */
    @SentinelResource(value = "sendMessage", blockHandler = "sendMessageBlockHandler")
    @CircuitBreaker(name = "a1688-api", fallbackMethod = "sendMessageFallback")
    @Retry(name = "a1688-api")
    public BaseResponse sendMessage(Long shopId, MessageRequest request) {
        log.info("发送消息: shopId={}, toMemberId={}", shopId, request.getToMemberId());
        
        try {
            Map<String, Object> apiParams = buildApiParams(shopId, "alibaba.icbu.message.send", request);
            
            String response = executeApiCall(apiParams);
            
            BaseResponse baseResponse = objectMapper.readValue(response, BaseResponse.class);
            log.info("消息发送成功: toMemberId={}", request.getToMemberId());
            
            return baseResponse;
        } catch (Exception e) {
            log.error("发送消息失败", e);
            throw new RuntimeException("发送消息失败", e);
        }
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 构建API请求参数
     * 
     * @param shopId 店铺ID
     * @param method API方法名
     * @param bizParams 业务参数
     * @return 完整的API参数
     */
    private Map<String, Object> buildApiParams(Long shopId, String method, Object bizParams) {
        Map<String, Object> params = new TreeMap<>();
        params.put("app_key", config.getAppKey());
        params.put("method", method);
        params.put("v", API_VERSION);
        params.put("sign_method", SIGN_METHOD);
        params.put("format", FORMAT);
        params.put("timestamp", Instant.now().toString());
        
        // 获取Access Token
        A1688Config.AccessTokenInfo tokenInfo = config.getAccessToken(shopId);
        if (tokenInfo != null && !tokenInfo.isExpired()) {
            params.put("access_token", tokenInfo.getAccessToken());
        }
        
        // 序列化业务参数
        if (bizParams != null) {
            try {
                String bizContent = objectMapper.writeValueAsString(bizParams);
                params.put("biz_content", bizContent);
            } catch (Exception e) {
                log.error("序列化业务参数失败", e);
            }
        }
        
        // 生成签名
        String sign = generateSign(params);
        params.put("sign", sign);
        
        return params;
    }
    
    /**
     * 生成签名
     * 1688 API使用SHA256签名算法
     * 
     * @param params 请求参数
     * @return 签名字符串
     */
    private String generateSign(Map<String, Object> params) {
        try {
            // 移除sign参数
            Map<String, Object> sortedParams = new TreeMap<>(params);
            sortedParams.remove("sign");
            
            // 拼接签名字符串
            StringBuilder signStr = new StringBuilder(config.getAppSecret());
            
            for (Map.Entry<String, Object> entry : sortedParams.entrySet()) {
                if (entry.getValue() != null) {
                    signStr.append(entry.getKey()).append(entry.getValue());
                }
            }
            
            signStr.append(config.getAppSecret());
            
            // SHA256签名
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(signStr.toString().getBytes(StandardCharsets.UTF_8));
            
            // 转为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            log.error("生成签名失败", e);
            throw new RuntimeException("生成签名失败", e);
        }
    }
    
    /**
     * 执行API调用
     * 
     * @param params 请求参数
     * @return 响应字符串
     */
    private String executeApiCall(Map<String, Object> params) {
        try {
            HttpResponse response = HttpRequest.post(config.getApiUrl())
                    .form(params)
                    .timeout(30000)
                    .execute();
            
            if (!response.isOk()) {
                throw new RuntimeException("API调用失败: HTTP " + response.getStatus());
            }
            
            return response.body();
        } catch (Exception e) {
            log.error("API调用异常", e);
            throw new RuntimeException("API调用异常", e);
        }
    }
    
    /**
     * 执行POST请求
     * 
     * @param url 请求URL
     * @param params 请求参数
     * @return 响应字符串
     */
    private String executePost(String url, Map<String, String> params) {
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form(params)
                    .timeout(30000)
                    .execute();
            
            if (!response.isOk()) {
                throw new RuntimeException("请求失败: HTTP " + response.getStatus());
            }
            
            return response.body();
        } catch (Exception e) {
            log.error("POST请求异常: url={}", url, e);
            throw new RuntimeException("POST请求异常", e);
        }
    }
    
    // ==================== Sentinel Block Handlers ====================
    
    public TokenResponse getAccessTokenBlockHandler(String code, BlockException ex) {
        log.warn("获取Token被限流: code={}", code);
        throw new RuntimeException("服务繁忙，请稍后重试");
    }
    
    public TokenResponse getAccessTokenFallback(String code, Throwable t) {
        log.error("获取Token降级: code={}", code, t);
        return null;
    }
    
    public TokenResponse refreshTokenBlockHandler(String refreshToken, BlockException ex) {
        log.warn("刷新Token被限流");
        throw new RuntimeException("服务繁忙，请稍后重试");
    }
    
    public TokenResponse refreshTokenFallback(String refreshToken, Throwable t) {
        log.error("刷新Token降级", t);
        return null;
    }
    
    public ProductSearchResponse searchProductsBlockHandler(Long shopId, ProductSearchRequest request, BlockException ex) {
        log.warn("商品搜索被限流: shopId={}", shopId);
        throw new RuntimeException("服务繁忙，请稍后重试");
    }
    
    public ProductSearchResponse searchProductsFallback(Long shopId, ProductSearchRequest request, Throwable t) {
        log.error("商品搜索降级: shopId={}", shopId, t);
        return null;
    }
    
    public ProductDetailResponse getProductDetailBlockHandler(Long shopId, String productId, BlockException ex) {
        log.warn("获取商品详情被限流: productId={}", productId);
        throw new RuntimeException("服务繁忙，请稍后重试");
    }
    
    public ProductDetailResponse getProductDetailFallback(Long shopId, String productId, Throwable t) {
        log.error("获取商品详情降级: productId={}", productId, t);
        return null;
    }
    
    public PurchaseOrderResponse createPurchaseOrderBlockHandler(Long shopId, PurchaseOrderRequest request, BlockException ex) {
        log.warn("创建订单被限流: shopId={}", shopId);
        throw new RuntimeException("服务繁忙，请稍后重试");
    }
    
    public PurchaseOrderResponse createPurchaseOrderFallback(Long shopId, PurchaseOrderRequest request, Throwable t) {
        log.error("创建订单降级: shopId={}", shopId, t);
        return null;
    }
    
    public OrderDetailResponse getOrderDetailBlockHandler(Long shopId, String orderId, BlockException ex) {
        log.warn("获取订单详情被限流: orderId={}", orderId);
        throw new RuntimeException("服务繁忙，请稍后重试");
    }
    
    public OrderDetailResponse getOrderDetailFallback(Long shopId, String orderId, Throwable t) {
        log.error("获取订单详情降级: orderId={}", orderId, t);
        return null;
    }
    
    public OrderListResponse queryOrderListBlockHandler(Long shopId, OrderQueryRequest request, BlockException ex) {
        log.warn("查询订单列表被限流: shopId={}", shopId);
        throw new RuntimeException("服务繁忙，请稍后重试");
    }
    
    public OrderListResponse queryOrderListFallback(Long shopId, OrderQueryRequest request, Throwable t) {
        log.error("查询订单列表降级: shopId={}", shopId, t);
        return null;
    }
    
    public BaseResponse cancelOrderBlockHandler(Long shopId, String orderId, String reason, BlockException ex) {
        log.warn("取消订单被限流: orderId={}", orderId);
        throw new RuntimeException("服务繁忙，请稍后重试");
    }
    
    public BaseResponse cancelOrderFallback(Long shopId, String orderId, String reason, Throwable t) {
        log.error("取消订单降级: orderId={}", orderId, t);
        return null;
    }
    
    public BaseResponse confirmReceiveBlockHandler(Long shopId, String orderId, BlockException ex) {
        log.warn("确认收货被限流: orderId={}", orderId);
        throw new RuntimeException("服务繁忙，请稍后重试");
    }
    
    public BaseResponse confirmReceiveFallback(Long shopId, String orderId, Throwable t) {
        log.error("确认收货降级: orderId={}", orderId, t);
        return null;
    }
    
    public BaseResponse sendMessageBlockHandler(Long shopId, MessageRequest request, BlockException ex) {
        log.warn("发送消息被限流: shopId={}", shopId);
        throw new RuntimeException("服务繁忙，请稍后重试");
    }
    
    public BaseResponse sendMessageFallback(Long shopId, MessageRequest request, Throwable t) {
        log.error("发送消息降级: shopId={}", shopId, t);
        return null;
    }
}