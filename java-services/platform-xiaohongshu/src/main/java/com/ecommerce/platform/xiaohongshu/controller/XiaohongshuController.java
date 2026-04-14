package com.ecommerce.platform.xiaohongshu.controller;

import com.ecommerce.platform.xiaohongshu.dto.*;
import com.ecommerce.platform.xiaohongshu.service.XiaohongshuAuthService;
import com.ecommerce.platform.xiaohongshu.service.XiaohongshuMessageService;
import com.ecommerce.platform.xiaohongshu.service.XiaohongshuOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 小红书REST API控制器
 * 
 * 提供以下接口:
 * - 授权管理
 * - 订单管理
 * - 商品管理
 * - 消息发送
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/xiaohongshu")
@RequiredArgsConstructor
public class XiaohongshuController {

    private final XiaohongshuAuthService authService;
    private final XiaohongshuOrderService orderService;
    private final XiaohongshuMessageService messageService;

    // ==================== 授权管理 ====================

    /**
     * 获取授权URL
     */
    @GetMapping("/auth/url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAuthorizationUrl(
            @RequestParam Long shopId,
            @RequestParam(required = false) String redirectUri) {
        
        log.info("获取小红书授权URL: shopId={}", shopId);

        String authUrl = authService.getAuthorizationUrl(shopId, redirectUri);

        Map<String, String> data = new HashMap<>();
        data.put("authUrl", authUrl);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 获取授权状态
     */
    @GetMapping("/auth/status")
    public ResponseEntity<ApiResponse<XiaohongshuAuthService.AuthStatus>> getAuthStatus(
            @RequestParam Long shopId) {
        
        log.info("获取小红书授权状态: shopId={}", shopId);

        XiaohongshuAuthService.AuthStatus status = authService.getAuthStatus(shopId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    /**
     * 刷新Token
     */
    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<Boolean>> refreshToken(@RequestParam Long shopId) {
        log.info("刷新小红书Token: shopId={}", shopId);

        boolean success = authService.refreshToken(shopId);
        return ResponseEntity.ok(ApiResponse.success(success));
    }

    /**
     * 取消授权
     */
    @DeleteMapping("/auth")
    public ResponseEntity<ApiResponse<Void>> revokeAuth(@RequestParam Long shopId) {
        log.info("取消小红书授权: shopId={}", shopId);

        authService.revokeAuth(shopId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ==================== 订单管理 ====================

    /**
     * 获取订单列表
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<OrderListResponse>> getOrderList(
            @RequestParam Long shopId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        
        log.info("获取小红书订单列表: shopId={}, page={}, pageSize={}", shopId, page, pageSize);

        OrderListResponse response = orderService.getOrderList(shopId, page, pageSize, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @RequestParam Long shopId,
            @PathVariable String orderId) {
        
        log.info("获取小红书订单详情: shopId={}, orderId={}", shopId, orderId);

        OrderDetailResponse response = orderService.getOrderDetail(shopId, orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== 消息发送 ====================

    /**
     * 发送文本消息
     */
    @PostMapping("/messages/text")
    public ResponseEntity<ApiResponse<Boolean>> sendTextMessage(
            @RequestParam Long shopId,
            @RequestParam String toUserId,
            @RequestParam String conversationId,
            @RequestParam String text) {
        
        log.info("发送小红书文本消息: shopId={}, toUserId={}", shopId, toUserId);

        boolean success = messageService.sendTextMessage(shopId, toUserId, conversationId, text);
        return ResponseEntity.ok(ApiResponse.success(success));
    }

    /**
     * 发送图片消息
     */
    @PostMapping("/messages/image")
    public ResponseEntity<ApiResponse<Boolean>> sendImageMessage(
            @RequestParam Long shopId,
            @RequestParam String toUserId,
            @RequestParam String conversationId,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String mediaId) {
        
        log.info("发送小红书图片消息: shopId={}, toUserId={}", shopId, toUserId);

        boolean success = messageService.sendImageMessage(shopId, toUserId, conversationId, imageUrl, mediaId);
        return ResponseEntity.ok(ApiResponse.success(success));
    }

    /**
     * 发送商品卡片消息
     */
    @PostMapping("/messages/product-card")
    public ResponseEntity<ApiResponse<Boolean>> sendProductCardMessage(
            @RequestParam Long shopId,
            @RequestParam String toUserId,
            @RequestParam String conversationId,
            @RequestParam String productId,
            @RequestParam String productName,
            @RequestParam String productImage,
            @RequestParam Long price,
            @RequestParam String productUrl) {
        
        log.info("发送小红书商品卡片消息: shopId={}, toUserId={}, productId={}", shopId, toUserId, productId);

        boolean success = messageService.sendProductCardMessage(shopId, toUserId, conversationId,
                productId, productName, productImage, price, productUrl);
        return ResponseEntity.ok(ApiResponse.success(success));
    }

    /**
     * 发送自定义消息
     */
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<Boolean>> sendMessage(
            @RequestBody SendMessageRequest request) {
        
        log.info("发送小红书自定义消息: shopId={}, toUserId={}, msgType={}", 
                request.getShopId(), request.getToUserId(), request.getMsgType());

        boolean success = messageService.sendMessage(request.getShopId(), request);
        return ResponseEntity.ok(ApiResponse.success(success));
    }

    // ==================== 健康检查 ====================

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("platform", "xiaohongshu");
        data.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}