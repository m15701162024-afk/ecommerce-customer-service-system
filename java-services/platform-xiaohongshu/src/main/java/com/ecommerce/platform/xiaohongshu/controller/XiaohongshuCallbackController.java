package com.ecommerce.platform.xiaohongshu.controller;

import com.ecommerce.platform.xiaohongshu.config.XiaohongshuConfig;
import com.ecommerce.platform.xiaohongshu.service.XiaohongshuAuthService;
import com.ecommerce.platform.xiaohongshu.service.XiaohongshuMessageService;
import com.ecommerce.platform.xiaohongshu.service.XiaohongshuOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/**
 * 小红书回调控制器
 * 
 * 处理:
 * - 消息回调
 * - 订单状态变更回调
 * - 授权回调
 * - 回调验证
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/platform/xiaohongshu")
@RequiredArgsConstructor
public class XiaohongshuCallbackController {

    private final XiaohongshuMessageService messageService;
    private final XiaohongshuAuthService authService;
    private final XiaohongshuOrderService orderService;
    private final XiaohongshuConfig config;

    /**
     * 消息回调
     */
    @PostMapping("/callback/message")
    public ResponseEntity<String> handleMessage(@RequestBody Map<String, Object> payload) {
        log.info("收到小红书消息回调: {}", payload);

        try {
            // 验证签名
            if (!verifySignature(payload)) {
                log.warn("签名验证失败");
                return ResponseEntity.ok("{\"code\":-1,\"msg\":\"签名验证失败\"}");
            }

            String type = (String) payload.get("type");

            switch (type) {
                case "chat_message" -> messageService.handleChatMessage(payload);
                case "order_status_change" -> {
                    messageService.handleOrderStatusChange(payload);
                    // 同时处理订单状态变更
                    Long shopId = getLongValue(payload, "shop_id");
                    String orderId = (String) payload.get("order_id");
                    Integer newStatus = getIntegerValue(payload, "new_status");
                    orderService.handleOrderStatusChange(shopId, orderId, newStatus);
                }
                default -> log.warn("未知事件类型: {}", type);
            }

            return ResponseEntity.ok("{\"code\":0,\"msg\":\"success\"}");
        } catch (Exception e) {
            log.error("处理消息回调失败", e);
            return ResponseEntity.ok("{\"code\":-1,\"msg\":\"处理失败\"}");
        }
    }

    /**
     * 回调验证 (用于验证回调URL的有效性)
     */
    @GetMapping("/callback/verify")
    public ResponseEntity<String> verifyCallback(
            @RequestParam(required = false) String signature,
            @RequestParam(required = false) String timestamp,
            @RequestParam(required = false) String nonce,
            @RequestParam(required = false) String echostr) {
        
        log.info("小红书回调验证: signature={}, timestamp={}, nonce={}", signature, timestamp, nonce);

        // 验证签名
        if (signature != null && timestamp != null && nonce != null) {
            if (verifyCallbackSignature(signature, timestamp, nonce)) {
                log.info("回调验证成功");
                return ResponseEntity.ok(echostr != null ? echostr : "success");
            } else {
                log.warn("回调验证失败");
                return ResponseEntity.ok("fail");
            }
        }

        // 简单验证模式
        return ResponseEntity.ok(echostr != null ? echostr : "success");
    }

    /**
     * 授权回调
     */
    @GetMapping("/auth/callback")
    public ResponseEntity<String> handleAuthCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description) {
        
        log.info("小红书授权回调: code={}, state={}, error={}", code, state, error);

        // 用户拒绝授权
        if (error != null) {
            log.warn("用户拒绝授权: error={}, description={}", error, error_description);
            return ResponseEntity.ok("授权失败: " + (error_description != null ? error_description : error));
        }

        // 处理授权成功
        if (code != null && state != null) {
            boolean success = authService.handleAuthCallback(code, state);
            if (success) {
                return ResponseEntity.ok("授权成功！您可以关闭此页面。");
            } else {
                return ResponseEntity.ok("授权处理失败，请稍后重试。");
            }
        }

        return ResponseEntity.ok("无效的授权请求");
    }

    /**
     * 订单状态变更回调
     */
    @PostMapping("/callback/order")
    public ResponseEntity<String> handleOrderCallback(@RequestBody Map<String, Object> payload) {
        log.info("收到小红书订单回调: {}", payload);

        try {
            // 验证签名
            if (!verifySignature(payload)) {
                log.warn("签名验证失败");
                return ResponseEntity.ok("{\"code\":-1,\"msg\":\"签名验证失败\"}");
            }

            Long shopId = getLongValue(payload, "shop_id");
            String orderId = (String) payload.get("order_id");
            Integer newStatus = getIntegerValue(payload, "new_status");

            orderService.handleOrderStatusChange(shopId, orderId, newStatus);

            return ResponseEntity.ok("{\"code\":0,\"msg\":\"success\"}");
        } catch (Exception e) {
            log.error("处理订单回调失败", e);
            return ResponseEntity.ok("{\"code\":-1,\"msg\":\"处理失败\"}");
        }
    }

    /**
     * 商品状态变更回调
     */
    @PostMapping("/callback/product")
    public ResponseEntity<String> handleProductCallback(@RequestBody Map<String, Object> payload) {
        log.info("收到小红书商品回调: {}", payload);

        try {
            // 验证签名
            if (!verifySignature(payload)) {
                log.warn("签名验证失败");
                return ResponseEntity.ok("{\"code\":-1,\"msg\":\"签名验证失败\"}");
            }

            // TODO: 实现商品状态变更处理
            String productId = (String) payload.get("product_id");
            String action = (String) payload.get("action");

            log.info("商品状态变更: productId={}, action={}", productId, action);

            return ResponseEntity.ok("{\"code\":0,\"msg\":\"success\"}");
        } catch (Exception e) {
            log.error("处理商品回调失败", e);
            return ResponseEntity.ok("{\"code\":-1,\"msg\":\"处理失败\"}");
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 验证签名
     */
    private boolean verifySignature(Map<String, Object> payload) {
        String signature = (String) payload.get("signature");
        String timestamp = (String) payload.get("timestamp");
        String nonce = (String) payload.get("nonce");

        if (signature == null || timestamp == null || nonce == null) {
            // 如果没有签名参数，默认通过（开发环境）
            return true;
        }

        return verifyCallbackSignature(signature, timestamp, nonce);
    }

    /**
     * 验证回调签名
     */
    private boolean verifyCallbackSignature(String signature, String timestamp, String nonce) {
        try {
            // 拼接字符串并排序
            String[] arr = {config.getAppSecret(), timestamp, nonce};
            java.util.Arrays.sort(arr);

            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                sb.append(s);
            }

            // SHA1加密
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString().equals(signature);
        } catch (Exception e) {
            log.error("验证签名失败", e);
            return false;
        }
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}