package com.ecommerce.platform.douyin.controller;

import com.ecommerce.common.util.SignatureUtil;
import com.ecommerce.platform.douyin.config.DouyinConfig;
import com.ecommerce.platform.douyin.service.DouyinMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/platform/douyin")
@RequiredArgsConstructor
public class DouyinCallbackController {
    
    private final DouyinMessageService messageService;
    private final DouyinConfig douyinConfig;
    private final ObjectMapper objectMapper;
    
    @PostMapping("/callback/message")
    public ResponseEntity<String> handleMessage(
            @RequestParam(required = false) String signature,
            @RequestParam(required = false) String timestamp,
            @RequestParam(required = false) String nonce,
            @RequestBody Map<String, Object> payload) {
        log.info("收到抖音消息回调: signature={}, timestamp={}, nonce={}, payload={}", 
                signature, timestamp, nonce, payload);
        
        if (signature == null || timestamp == null || nonce == null) {
            log.warn("缺少签名验证参数");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"err_no\":400,\"err_msg\":\"missing signature parameters\"}");
        }
        
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String dataToSign = SignatureUtil.buildSignatureData(timestamp, nonce, payloadJson);
            
            if (!SignatureUtil.verifySignature(dataToSign, signature, douyinConfig.getAppSecret())) {
                log.warn("签名验证失败: signature={}", signature);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"err_no\":403,\"err_msg\":\"signature verification failed\"}");
            }
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"err_no\":500,\"err_msg\":\"signature verification error\"}");
        }
        
        String eventType = (String) payload.get("type");
        
        switch (eventType) {
            case "im_receive_msg" -> messageService.handleChatMessage(payload);
            case "order_status_change" -> messageService.handleOrderStatusChange(payload);
            default -> log.warn("未知事件类型: {}", eventType);
        }
        
        return ResponseEntity.ok("{\"err_no\":0,\"err_msg\":\"success\"}");
    }
    
    @GetMapping("/callback/verify")
    public ResponseEntity<String> verifyCallback(
            @RequestParam String signature,
            @RequestParam String timestamp,
            @RequestParam String nonce,
            @RequestParam String echostr
    ) {
        log.info("抖音回调验证: signature={}, timestamp={}, nonce={}", signature, timestamp, nonce);
        
        String dataToSign = SignatureUtil.buildSignatureData(timestamp, nonce, echostr);
        if (!SignatureUtil.verifySignature(dataToSign, signature, douyinConfig.getAppSecret())) {
            log.warn("回调验证签名失败");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("signature verification failed");
        }
        
        return ResponseEntity.ok(echostr);
    }
    
    @PostMapping("/auth/callback")
    public ResponseEntity<String> handleAuthCallback(@RequestParam String code, @RequestParam String state) {
        log.info("抖音授权回调: code={}, state={}", code, state);
        
        messageService.handleAuthCode(code, state);
        
        return ResponseEntity.ok("授权成功");
    }
}