package com.ecommerce.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Mock SMS Provider
 * 
 * Mock implementation for development and testing.
 * Logs the code instead of actually sending SMS.
 * Active when "dev" or "test" profile is enabled.
 */
@Component
@Profile({"dev", "test", "default"})
@Slf4j
public class MockSmsProvider implements SmsProvider {
    
    @Override
    public boolean sendSms(String phone, String code) {
        log.info("=== MOCK SMS === Phone: {}, Code: {} ===", phone, code);
        // In development, always return success
        return true;
    }
    
    @Override
    public String getProviderName() {
        return "mock";
    }
}