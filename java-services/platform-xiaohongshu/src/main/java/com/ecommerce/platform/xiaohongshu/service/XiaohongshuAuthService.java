package com.ecommerce.platform.xiaohongshu.service;

import com.ecommerce.platform.xiaohongshu.client.XiaohongshuApiClient;
import com.ecommerce.platform.xiaohongshu.config.XiaohongshuConfig;
import com.ecommerce.platform.xiaohongshu.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 小红书授权服务
 * 
 * 功能:
 * - OAuth2.0授权认证
 * - Token管理
 * - Token自动刷新
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XiaohongshuAuthService {

    private final XiaohongshuApiClient apiClient;
    private final XiaohongshuConfig config;

    /**
     * 店铺授权状态缓存 (shopId -> 授权状态)
     */
    private final Map<Long, AuthStatus> authStatusMap = new ConcurrentHashMap<>();

    /**
     * 获取授权URL
     * 
     * @param shopId 店铺ID
     * @param redirectUri 回调地址
     * @return 授权URL
     */
    public String getAuthorizationUrl(Long shopId, String redirectUri) {
        log.info("生成小红书授权URL: shopId={}", shopId);
        
        String state = generateState(shopId);
        return apiClient.getAuthorizationUrl(state, redirectUri);
    }

    /**
     * 处理授权回调
     * 
     * @param code 授权码
     * @param state 状态参数
     * @return 是否成功
     */
    public boolean handleAuthCallback(String code, String state) {
        log.info("处理小红书授权回调: code={}, state={}", code, state);

        try {
            Long shopId = parseShopIdFromState(state);
            if (shopId == null) {
                log.error("无效的state参数: {}", state);
                return false;
            }

            TokenResponse response = apiClient.getAccessToken(code);

            if (response != null && response.isSuccess()) {
                TokenResponse.Data data = response.getData();
                config.setTokenInfo(shopId, data.getAccessToken(), data.getRefreshToken(), data.getExpiresIn());

                // 更新授权状态
                AuthStatus status = new AuthStatus();
                status.setShopId(shopId);
                status.setAuthorized(true);
                status.setUserId(data.getUserId());
                status.setOpenId(data.getOpenId());
                status.setAuthorizedAt(System.currentTimeMillis());
                authStatusMap.put(shopId, status);

                log.info("小红书授权成功: shopId={}, userId={}", shopId, data.getUserId());
                return true;
            } else {
                log.error("小红书授权失败: code={}, msg={}", 
                        response != null ? response.getCode() : null,
                        response != null ? response.getMsg() : null);
                return false;
            }
        } catch (Exception e) {
            log.error("处理授权回调失败", e);
            return false;
        }
    }

    /**
     * 刷新Token
     * 
     * @param shopId 店铺ID
     * @return 是否成功
     */
    public boolean refreshToken(Long shopId) {
        log.info("刷新小红书Token: shopId={}", shopId);

        String refreshToken = config.getRefreshToken(shopId);
        if (refreshToken == null) {
            log.error("Refresh Token不存在: shopId={}", shopId);
            return false;
        }

        try {
            TokenResponse response = apiClient.refreshToken(shopId, refreshToken);

            if (response != null && response.isSuccess()) {
                log.info("Token刷新成功: shopId={}", shopId);
                return true;
            } else {
                log.error("Token刷新失败: shopId={}, code={}, msg={}",
                        shopId,
                        response != null ? response.getCode() : null,
                        response != null ? response.getMsg() : null);
                return false;
            }
        } catch (Exception e) {
            log.error("Token刷新异常: shopId={}", shopId, e);
            return false;
        }
    }

    /**
     * 检查店铺授权状态
     * 
     * @param shopId 店铺ID
     * @return 是否已授权
     */
    public boolean isAuthorized(Long shopId) {
        // 检查Token是否存在且有效
        String accessToken = config.getAccessToken(shopId);
        if (accessToken != null) {
            return true;
        }

        // 尝试刷新Token
        AuthStatus status = authStatusMap.get(shopId);
        if (status != null && status.isAuthorized()) {
            return refreshToken(shopId);
        }

        return false;
    }

    /**
     * 获取授权状态
     * 
     * @param shopId 店铺ID
     * @return 授权状态
     */
    public AuthStatus getAuthStatus(Long shopId) {
        AuthStatus status = authStatusMap.get(shopId);
        if (status == null) {
            status = new AuthStatus();
            status.setShopId(shopId);
            status.setAuthorized(false);
        }
        return status;
    }

    /**
     * 取消授权
     * 
     * @param shopId 店铺ID
     */
    public void revokeAuth(Long shopId) {
        log.info("取消小红书授权: shopId={}", shopId);
        
        config.removeToken(shopId);
        authStatusMap.remove(shopId);
    }

    /**
     * 定时刷新即将过期的Token (每30分钟检查一次)
     */
    @Scheduled(fixedRate = 1800000)
    public void refreshExpiringTokens() {
        log.debug("检查需要刷新的Token...");

        for (Map.Entry<Long, AuthStatus> entry : authStatusMap.entrySet()) {
            Long shopId = entry.getKey();
            AuthStatus status = entry.getValue();

            if (!status.isAuthorized()) {
                continue;
            }

            // 检查Token是否即将过期 (提前30分钟刷新)
            String accessToken = config.getAccessToken(shopId);
            if (accessToken == null) {
                log.info("Token已过期，尝试刷新: shopId={}", shopId);
                refreshToken(shopId);
            }
        }
    }

    /**
     * 生成state参数
     */
    private String generateState(Long shopId) {
        return String.format("shop_%d_%d", shopId, System.currentTimeMillis());
    }

    /**
     * 从state解析店铺ID
     */
    private Long parseShopIdFromState(String state) {
        try {
            if (state != null && state.startsWith("shop_")) {
                String[] parts = state.split("_");
                if (parts.length >= 2) {
                    return Long.parseLong(parts[1]);
                }
            }
        } catch (Exception e) {
            log.error("解析state失败: {}", state, e);
        }
        return null;
    }

    /**
     * 授权状态
     */
    @lombok.Data
    public static class AuthStatus {
        private Long shopId;
        private boolean authorized;
        private String userId;
        private String openId;
        private Long authorizedAt;
    }
}