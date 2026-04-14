package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.SendSmsRequest;
import com.ecommerce.auth.service.SmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SMS Controller
 * 
 * REST API endpoints for SMS verification code operations.
 */
@RestController
@RequestMapping("/auth/sms")
@RequiredArgsConstructor
@Slf4j
public class SmsController {
    
    private final SmsService smsService;
    
    /**
     * Send verification code endpoint
     * 
     * POST /auth/sms/send
     * Request body: { "phone": "+8613800138000" }
     * Response: { "message": "Verification code sent successfully" }
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendCode(@Valid @RequestBody SendSmsRequest request) {
        log.info("Send SMS code request for phone: {}", maskPhone(request.getPhone()));
        
        boolean sent = smsService.sendVerificationCode(request.getPhone());
        
        if (sent) {
            return ResponseEntity.ok(Map.of("message", "Verification code sent successfully"));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Failed to send verification code. Please check phone format or wait if rate limited."));
        }
    }
    
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "****" + phone.substring(phone.length() - 4);
    }
}