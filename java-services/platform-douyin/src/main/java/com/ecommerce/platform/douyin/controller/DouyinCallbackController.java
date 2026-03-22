package com.ecommerce.platform.douyin.controller;

import com.ecommerce.platform.douyin.service.DouyinMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/platform/douyin")
@RequiredArgsConstructor
public class DouyinCallbackController {
    
    private final DouyinMessageService messageService;
    
    @PostMapping("/callback/message")
    public ResponseEntity<String> handleMessage(@RequestBody Map<String, Object> payload) {
        log.info("收到抖音消息回调: {}", payload);
        
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
        return ResponseEntity.ok(echostr);
    }
    
    @PostMapping("/auth/callback")
    public ResponseEntity<String> handleAuthCallback(@RequestParam String code, @RequestParam String state) {
        log.info("抖音授权回调: code={}, state={}", code, state);
        
        messageService.handleAuthCode(code, state);
        
        return ResponseEntity.ok("授权成功");
    }
}