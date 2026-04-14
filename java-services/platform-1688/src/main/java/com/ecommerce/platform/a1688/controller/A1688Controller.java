package com.ecommerce.platform.a1688.controller;

import com.ecommerce.common.util.SignatureUtil;
import com.ecommerce.platform.a1688.config.A1688Config;
import com.ecommerce.platform.a1688.dto.request.MessageRequest;
import com.ecommerce.platform.a1688.dto.request.OrderQueryRequest;
import com.ecommerce.platform.a1688.dto.request.ProductSearchRequest;
import com.ecommerce.platform.a1688.dto.request.PurchaseOrderRequest;
import com.ecommerce.platform.a1688.dto.response.BaseResponse;
import com.ecommerce.platform.a1688.dto.response.OrderDetailResponse;
import com.ecommerce.platform.a1688.dto.response.OrderListResponse;
import com.ecommerce.platform.a1688.dto.response.ProductDetailResponse;
import com.ecommerce.platform.a1688.dto.response.ProductSearchResponse;
import com.ecommerce.platform.a1688.dto.response.PurchaseOrderResponse;
import com.ecommerce.platform.a1688.service.A1688ProductService;
import com.ecommerce.platform.a1688.service.A1688PurchaseService;
import com.ecommerce.platform.a1688.service.A1688Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 1688平台REST控制器
 * 
 * 提供以下API端点：
 * - 授权管理
 * - 商品搜索和详情
 * - 采购订单管理
 * - 消息推送
 * - 回调处理
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/platform/1688")
@RequiredArgsConstructor
public class A1688Controller {
    
    private final A1688Service a1688Service;
    private final A1688ProductService productService;
    private final A1688PurchaseService purchaseService;
    private final A1688Config a1688Config;
    private final ObjectMapper objectMapper;
    
    // ==================== 授权管理 ====================
    
    /**
     * 获取授权URL
     * 
     * @param shopId 店铺ID
     * @return 授权URL
     */
    @GetMapping("/auth/url")
    public ResponseEntity<ApiResponse<String>> getAuthUrl(@RequestParam Long shopId) {
        String authUrl = a1688Service.generateAuthUrl(shopId);
        return ResponseEntity.ok(ApiResponse.success(authUrl));
    }
    
    /**
     * 处理授权回调
     * 
     * @param code 授权码
     * @param state 状态参数
     * @return 处理结果
     */
    @GetMapping("/auth/callback")
    public ResponseEntity<ApiResponse<String>> handleAuthCallback(
            @RequestParam String code,
            @RequestParam String state) {
        a1688Service.handleAuthCallback(code, state);
        return ResponseEntity.ok(ApiResponse.success("授权成功"));
    }
    
    /**
     * 刷新Token
     * 
     * @param shopId 店铺ID
     * @return 刷新结果
     */
    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<Boolean>> refreshToken(@RequestParam Long shopId) {
        boolean success = a1688Service.refreshToken(shopId);
        return ResponseEntity.ok(ApiResponse.success(success));
    }
    
    // ==================== 商品相关 ====================
    
    /**
     * 搜索商品
     * 
     * @param shopId 店铺ID
     * @param request 搜索请求
     * @return 搜索结果
     */
    @PostMapping("/products/search")
    public ResponseEntity<ApiResponse<ProductSearchResponse>> searchProducts(
            @RequestHeader("X-Shop-Id") Long shopId,
            @Valid @RequestBody ProductSearchRequest request) {
        ProductSearchResponse response = productService.searchProducts(shopId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 简单搜索商品
     * 
     * @param shopId 店铺ID
     * @param keyword 关键词
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 搜索结果
     */
    @GetMapping("/products/search")
    public ResponseEntity<ApiResponse<ProductSearchResponse>> searchProductsSimple(
            @RequestHeader("X-Shop-Id") Long shopId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        ProductSearchResponse response = productService.searchProducts(shopId, keyword, pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 获取商品详情
     * 
     * @param shopId 店铺ID
     * @param productId 商品ID
     * @return 商品详情
     */
    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(
            @RequestHeader("X-Shop-Id") Long shopId,
            @PathVariable String productId) {
        ProductDetailResponse response = productService.getProductDetail(shopId, productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 获取相似商品
     * 
     * @param shopId 店铺ID
     * @param productId 商品ID
     * @param size 数量
     * @return 相似商品列表
     */
    @GetMapping("/products/{productId}/similar")
    public ResponseEntity<ApiResponse<ProductSearchResponse>> getSimilarProducts(
            @RequestHeader("X-Shop-Id") Long shopId,
            @PathVariable String productId,
            @RequestParam(defaultValue = "10") int size) {
        ProductSearchResponse response = productService.getSimilarProducts(shopId, productId, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 刷新商品缓存
     * 
     * @param productId 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/products/{productId}/cache")
    public ResponseEntity<ApiResponse<String>> refreshProductCache(@PathVariable String productId) {
        productService.refreshProductDetailCache(productId);
        return ResponseEntity.ok(ApiResponse.success("缓存已刷新"));
    }
    
    // ==================== 订单相关 ====================
    
    /**
     * 创建采购订单
     * 
     * @param shopId 店铺ID
     * @param request 订单请求
     * @return 订单响应
     */
    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> createOrder(
            @RequestHeader("X-Shop-Id") Long shopId,
            @Valid @RequestBody PurchaseOrderRequest request) {
        PurchaseOrderResponse response = purchaseService.createOrder(shopId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 获取订单详情
     * 
     * @param shopId 店铺ID
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @RequestHeader("X-Shop-Id") Long shopId,
            @PathVariable String orderId) {
        OrderDetailResponse response = purchaseService.getOrderDetail(shopId, orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 查询订单列表
     * 
     * @param shopId 店铺ID
     * @param request 查询请求
     * @return 订单列表
     */
    @PostMapping("/orders/query")
    public ResponseEntity<ApiResponse<OrderListResponse>> queryOrders(
            @RequestHeader("X-Shop-Id") Long shopId,
            @RequestBody OrderQueryRequest request) {
        OrderListResponse response = purchaseService.queryOrderList(shopId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 按状态查询订单
     * 
     * @param shopId 店铺ID
     * @param status 订单状态
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<OrderListResponse>> queryOrdersByStatus(
            @RequestHeader("X-Shop-Id") Long shopId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        OrderListResponse response = purchaseService.queryOrdersByStatus(shopId, status, pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 取消订单
     * 
     * @param shopId 店铺ID
     * @param orderId 订单ID
     * @param reason 取消原因
     * @return 操作结果
     */
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<ApiResponse<BaseResponse>> cancelOrder(
            @RequestHeader("X-Shop-Id") Long shopId,
            @PathVariable String orderId,
            @RequestParam(required = false, defaultValue = "买家取消") String reason) {
        BaseResponse response = purchaseService.cancelOrder(shopId, orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 确认收货
     * 
     * @param shopId 店铺ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PostMapping("/orders/{orderId}/confirm")
    public ResponseEntity<ApiResponse<BaseResponse>> confirmReceive(
            @RequestHeader("X-Shop-Id") Long shopId,
            @PathVariable String orderId) {
        BaseResponse response = purchaseService.confirmReceive(shopId, orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    // ==================== 消息相关 ====================
    
    /**
     * 发送消息给供应商
     * 
     * @param shopId 店铺ID
     * @param request 消息请求
     * @return 发送结果
     */
    @PostMapping("/messages/send")
    public ResponseEntity<ApiResponse<Boolean>> sendMessage(
            @RequestHeader("X-Shop-Id") Long shopId,
            @Valid @RequestBody MessageRequest request) {
        boolean success = a1688Service.sendMessage(shopId, request.getToMemberId(), request.getContent());
        return ResponseEntity.ok(ApiResponse.success(success));
    }
    
    /**
     * 发送文本消息
     * 
     * @param shopId 店铺ID
     * @param toMemberId 供应商会员ID
     * @param content 消息内容
     * @return 发送结果
     */
    @PostMapping("/messages/text")
    public ResponseEntity<ApiResponse<Boolean>> sendTextMessage(
            @RequestHeader("X-Shop-Id") Long shopId,
            @RequestParam String toMemberId,
            @RequestParam String content) {
        boolean success = a1688Service.sendMessage(shopId, toMemberId, content);
        return ResponseEntity.ok(ApiResponse.success(success));
    }
    
    // ==================== 回调处理 ====================
    
    /**
     * 处理消息回调
     * 
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param payload 回调数据
     * @return 处理结果
     */
    @PostMapping("/callback/message")
    public ResponseEntity<String> handleMessageCallback(
            @RequestParam(required = false) String signature,
            @RequestParam(required = false) String timestamp,
            @RequestParam(required = false) String nonce,
            @RequestBody Map<String, Object> payload) {
        log.info("收到1688消息回调: signature={}, timestamp={}, nonce={}, payload={}", 
                signature, timestamp, nonce, payload);
        
        if (signature == null || timestamp == null || nonce == null) {
            log.warn("缺少签名验证参数");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"errorCode\":400,\"errorMessage\":\"missing signature parameters\"}");
        }
        
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String dataToSign = SignatureUtil.buildSignatureData(timestamp, nonce, payloadJson);
            
            if (!SignatureUtil.verifySignature(dataToSign, signature, a1688Config.getAppSecret())) {
                log.warn("签名验证失败: signature={}", signature);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"errorCode\":403,\"errorMessage\":\"signature verification failed\"}");
            }
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"errorCode\":500,\"errorMessage\":\"signature verification error\"}");
        }
        
        String eventType = (String) payload.get("type");
        
        if (eventType != null) {
            switch (eventType) {
                case "im_receive_msg" -> a1688Service.handleChatMessage(payload);
                case "order_status_change" -> a1688Service.handleOrderStatusChange(payload);
                default -> log.warn("未知事件类型: {}", eventType);
            }
        }
        
        return ResponseEntity.ok("{\"errorCode\":0,\"errorMessage\":\"success\"}");
    }
    
    /**
     * 回调验证
     * 
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param echostr 回显字符串
     * @return 验证结果
     */
    @GetMapping("/callback/verify")
    public ResponseEntity<String> verifyCallback(
            @RequestParam(required = false) String signature,
            @RequestParam(required = false) String timestamp,
            @RequestParam(required = false) String nonce,
            @RequestParam(required = false) String echostr) {
        log.info("1688回调验证: signature={}, timestamp={}, nonce={}", signature, timestamp, nonce);
        
        if (signature == null || timestamp == null || nonce == null || echostr == null) {
            log.warn("回调验证参数缺失");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing parameters");
        }
        
        String dataToSign = SignatureUtil.buildSignatureData(timestamp, nonce, echostr);
        if (!SignatureUtil.verifySignature(dataToSign, signature, a1688Config.getAppSecret())) {
            log.warn("回调验证签名失败");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("signature verification failed");
        }
        
        return ResponseEntity.ok(echostr);
    }
    
    // ==================== 响应封装 ====================
    
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ApiResponse<T> {
        private Integer code;
        private String message;
        private T data;
        
        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(0, "success", data);
        }
        
        public static <T> ApiResponse<T> error(Integer code, String message) {
            return new ApiResponse<>(code, message, null);
        }
    }
}