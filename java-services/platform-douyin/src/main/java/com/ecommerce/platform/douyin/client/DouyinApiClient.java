package com.ecommerce.platform.douyin.client;

import com.ecommerce.platform.douyin.config.DouyinConfig;
import com.ecommerce.platform.douyin.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DouyinApiClient {
    
    private final DouyinConfig config;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AccessTokenResponse getAccessToken(String code) {
        String url = config.getApiUrl() + "/api/apps/v2/token";
        
        Map<String, String> params = new HashMap<>();
        params.put("appid", config.getAppId());
        params.put("secret", config.getAppSecret());
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(params), headers);
            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, AccessTokenResponse.class);
            
            return response.getBody();
        } catch (Exception e) {
            log.error("获取抖音AccessToken失败", e);
            return null;
        }
    }
    
    public OrderListResponse getOrderList(Long shopId, int page, int size, String startTime, String endTime) {
        String url = "https://op.jinritemai.com/order/searchList";
        
        Map<String, Object> params = new TreeMap<>();
        params.put("app_id", config.getAppId());
        params.put("method", "order.searchList");
        params.put("timestamp", System.currentTimeMillis() / 1000);
        params.put("v", "2");
        params.put("page", page);
        params.put("size", size);
        params.put("start_time", startTime);
        params.put("end_time", endTime);
        
        String accessToken = config.getAccessToken(shopId);
        if (accessToken != null) {
            params.put("access_token", accessToken);
        }
        
        try {
            String sign = generateSign(params);
            params.put("sign", sign);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(params), headers);
            ResponseEntity<OrderListResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, OrderListResponse.class);
            
            return response.getBody();
        } catch (Exception e) {
            log.error("获取抖音订单列表失败", e);
            return null;
        }
    }
    
    public OrderDetailResponse getOrderDetail(Long shopId, String orderId) {
        String url = "https://op.jinritemai.com/order/orderDetail";
        
        Map<String, Object> params = new TreeMap<>();
        params.put("app_id", config.getAppId());
        params.put("method", "order.orderDetail");
        params.put("timestamp", System.currentTimeMillis() / 1000);
        params.put("v", "2");
        params.put("shop_order_id", orderId);
        
        String accessToken = config.getAccessToken(shopId);
        if (accessToken != null) {
            params.put("access_token", accessToken);
        }
        
        try {
            String sign = generateSign(params);
            params.put("sign", sign);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(params), headers);
            ResponseEntity<OrderDetailResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, OrderDetailResponse.class);
            
            return response.getBody();
        } catch (Exception e) {
            log.error("获取抖音订单详情失败: {}", orderId, e);
            return null;
        }
    }
    
    public boolean sendMessage(Long shopId, String openId, String content, String msgType) {
        String url = config.getApiUrl() + "/api/apps/message/send/";
        
        Map<String, Object> params = new HashMap<>();
        params.put("app_id", config.getAppId());
        params.put("open_id", openId);
        params.put("msg_type", msgType);
        
        if ("text".equals(msgType)) {
            Map<String, String> contentMap = new HashMap<>();
            contentMap.put("text", content);
            params.put("content", contentMap);
        }
        
        String accessToken = config.getAccessToken(shopId);
        if (accessToken != null) {
            params.put("access_token", accessToken);
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(params), headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            if (response.getBody() != null) {
                Integer errNo = (Integer) response.getBody().get("err_no");
                return errNo != null && errNo == 0;
            }
        } catch (Exception e) {
            log.error("发送抖音消息失败", e);
        }
        return false;
    }
    
    private String generateSign(Map<String, Object> params) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(config.getAppSecret());
            
            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);
            
            for (String key : keys) {
                Object value = params.get(key);
                if (value != null && !"sign".equals(key)) {
                    sb.append(key).append(value);
                }
            }
            
            sb.append(config.getAppSecret());
            
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            log.error("生成签名失败", e);
            return "";
        }
    }
}