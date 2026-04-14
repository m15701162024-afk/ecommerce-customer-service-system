package com.ecommerce.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private final SmsProvider smsProvider;
    
    private final Map<String, String> codeStorage = new ConcurrentHashMap<>();
    private final Map<String, Long> rateLimitStorage = new ConcurrentHashMap<>();
    
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(5);
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();
    
    private static final String SMS_RATE_LIMIT_PREFIX = "sms:rate:";
    private static final Duration RATE_LIMIT_DURATION = Duration.ofMinutes(1);
    
    public boolean sendVerificationCode(String phone) {
        log.info("Sending verification code to phone: {}", maskPhone(phone));
        
        if (!isValidPhoneFormat(phone)) {
            log.warn("Invalid phone format: {}", maskPhone(phone));
            return false;
        }
        
        String rateLimitKey = SMS_RATE_LIMIT_PREFIX + phone;
        if (isRateLimited(rateLimitKey)) {
            log.warn("Rate limit exceeded for phone: {}", maskPhone(phone));
            return false;
        }
        
        String code = generateCode();
        
        String codeKey = SMS_CODE_PREFIX + phone;
        storeCode(codeKey, code);
        setRateLimit(rateLimitKey);
        
        boolean sent = smsProvider.sendSms(phone, code);
        
        if (sent) {
            log.info("Verification code sent successfully to: {}", maskPhone(phone));
        } else {
            log.error("Failed to send verification code to: {}", maskPhone(phone));
            deleteCode(codeKey);
        }
        
        return sent;
    }
    
    public boolean verifyCode(String phone, String code) {
        log.info("Verifying code for phone: {}", maskPhone(phone));
        
        if (phone == null || code == null) {
            return false;
        }
        
        String codeKey = SMS_CODE_PREFIX + phone;
        String storedCode = getCode(codeKey);
        
        if (storedCode == null) {
            log.warn("No code found for phone: {}", maskPhone(phone));
            return false;
        }
        
        boolean valid = storedCode.equals(code);
        
        if (valid) {
            deleteCode(codeKey);
            log.info("Code verified successfully for phone: {}", maskPhone(phone));
        } else {
            log.warn("Invalid code for phone: {}", maskPhone(phone));
        }
        
        return valid;
    }
    
    public Optional<String> getStoredCode(String phone) {
        String codeKey = SMS_CODE_PREFIX + phone;
        String storedCode = getCode(codeKey);
        return Optional.ofNullable(storedCode);
    }
    
    private boolean isRateLimited(String key) {
        if (redisTemplate != null) {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        }
        Long lastRequest = rateLimitStorage.get(key);
        return lastRequest != null && (System.currentTimeMillis() - lastRequest) < RATE_LIMIT_DURATION.toMillis();
    }
    
    private void setRateLimit(String key) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, "1", RATE_LIMIT_DURATION);
        } else {
            rateLimitStorage.put(key, System.currentTimeMillis());
        }
    }
    
    private void storeCode(String key, String code) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, code, CODE_EXPIRATION);
        } else {
            codeStorage.put(key, code);
        }
    }
    
    private String getCode(String key) {
        if (redisTemplate != null) {
            Object code = redisTemplate.opsForValue().get(key);
            return code != null ? code.toString() : null;
        }
        return codeStorage.get(key);
    }
    
    private void deleteCode(String key) {
        if (redisTemplate != null) {
            redisTemplate.delete(key);
        } else {
            codeStorage.remove(key);
        }
    }
    
    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
    }
    
    private boolean isValidPhoneFormat(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        String cleanPhone = phone.startsWith("+") ? phone.substring(1) : phone;
        return cleanPhone.matches("\\d{10,15}");
    }
    
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "****" + phone.substring(phone.length() - 4);
    }
}