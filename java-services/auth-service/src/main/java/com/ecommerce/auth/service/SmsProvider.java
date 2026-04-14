package com.ecommerce.auth.service;

/**
 * SMS Provider Interface
 * 
 * Abstraction for SMS service providers.
 * Supports implementation of various SMS providers like:
 * - Aliyun SMS (阿里云短信)
 * - Tencent Cloud SMS (腾讯云短信)
 * - Mock provider for testing
 */
public interface SmsProvider {
    
    /**
     * Send SMS verification code
     * 
     * @param phone target phone number (format: country code + number)
     * @param code verification code
     * @return true if sent successfully, false otherwise
     */
    boolean sendSms(String phone, String code);
    
    /**
     * Get provider name
     * 
     * @return provider name (e.g., "aliyun", "tencent", "mock")
     */
    String getProviderName();
}