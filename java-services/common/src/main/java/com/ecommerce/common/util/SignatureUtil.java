package com.ecommerce.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 签名工具类
 * 
 * 提供HMAC-SHA256签名生成和验证功能
 * 用于平台回调消息的签名验证，确保消息来源可信
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Slf4j
public class SignatureUtil {
    
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    
    /**
     * 生成HMAC-SHA256签名
     * 
     * @param data 待签名数据
     * @param secret 签名密钥
     * @return 十六进制编码的签名字符串
     */
    public static String generateSignature(String data, String secret) {
        if (data == null || secret == null) {
            log.warn("签名数据或密钥为空");
            return null;
        }
        
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), 
                    HMAC_SHA256_ALGORITHM
            );
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("HMAC-SHA256算法不可用", e);
            throw new RuntimeException("HMAC-SHA256算法不可用", e);
        } catch (InvalidKeyException e) {
            log.error("无效的签名密钥", e);
            throw new RuntimeException("无效的签名密钥", e);
        }
    }
    
    /**
     * 验证签名
     * 
     * @param data 待验证数据
     * @param signature 待验证的签名
     * @param secret 签名密钥
     * @return 签名是否有效
     */
    public static boolean verifySignature(String data, String signature, String secret) {
        if (data == null || signature == null || secret == null) {
            log.warn("签名验证参数为空: data={}, signature={}, secret={}", 
                    data != null, signature != null, secret != null);
            return false;
        }
        
        String expectedSignature = generateSignature(data, secret);
        if (expectedSignature == null) {
            return false;
        }
        
        // 使用安全的方式比较签名，防止时序攻击
        return secureEquals(expectedSignature, signature);
    }
    
    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * 安全比较两个字符串，防止时序攻击
     * 
     * @param a 字符串a
     * @param b 字符串b
     * @return 是否相等
     */
    private static boolean secureEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
    
    /**
     * 构建签名数据
     * 
     * 按照平台要求的格式拼接签名数据：
     * timestamp + nonce + payload
     * 
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param payload 请求体
     * @return 拼接后的待签名数据
     */
    public static String buildSignatureData(String timestamp, String nonce, String payload) {
        return timestamp + nonce + payload;
    }
}