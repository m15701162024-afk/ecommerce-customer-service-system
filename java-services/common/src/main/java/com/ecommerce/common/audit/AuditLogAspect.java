package com.ecommerce.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 审计日志切面
 */
@Slf4j
@Aspect
@Component
public class AuditLogAspect {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Around("@annotation(com.ecommerce.common.audit.AuditLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        AuditLog auditLog = method.getAnnotation(AuditLog.class);
        
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;
        
        try {
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            logAudit(point, auditLog, result, exception, duration);
        }
    }
    
    private void logAudit(ProceedingJoinPoint point, AuditLog auditLog, 
                          Object result, Throwable exception, long duration) {
        try {
            HttpServletRequest request = getRequest();
            Map<String, Object> auditInfo = buildAuditInfo(point, auditLog, request, result, exception, duration);
            String auditLogJson = objectMapper.writeValueAsString(auditInfo);
            log.info("[AUDIT_LOG] {}", auditLogJson);
        } catch (Exception e) {
            log.error("记录审计日志失败", e);
        }
    }
    
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
    
    private Map<String, Object> buildAuditInfo(ProceedingJoinPoint point, AuditLog auditLog,
                                                HttpServletRequest request, Object result, 
                                                Throwable exception, long duration) {
        Map<String, Object> auditInfo = new HashMap<>();
        
        auditInfo.put("timestamp", LocalDateTime.now().format(FORMATTER));
        auditInfo.put("module", auditLog.module());
        auditInfo.put("action", auditLog.action());
        auditInfo.put("description", auditLog.description());
        auditInfo.put("duration", duration + "ms");
        auditInfo.put("status", exception == null ? "SUCCESS" : "FAILED");
        
        if (request != null) {
            auditInfo.put("method", request.getMethod());
            auditInfo.put("uri", request.getRequestURI());
            auditInfo.put("ip", getClientIp(request));
            auditInfo.put("user", getUser(request));
        }
        
        auditInfo.put("className", point.getTarget().getClass().getSimpleName());
        auditInfo.put("methodName", point.getSignature().getName());
        
        try {
            Map<String, Object> params = getMethodParams(point);
            auditInfo.put("params", desensitizeParams(params));
        } catch (Exception e) {
            auditInfo.put("params", "参数解析失败");
        }
        
        if (exception != null) {
            auditInfo.put("error", exception.getMessage());
        }
        
        return auditInfo;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    private String getUser(HttpServletRequest request) {
        Object user = request.getAttribute("user");
        return user != null ? user.toString() : "anonymous";
    }
    
    private Map<String, Object> getMethodParams(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] paramValues = point.getArgs();
        
        Map<String, Object> params = new HashMap<>();
        if (paramNames != null && paramNames.length > 0) {
            for (int i = 0; i < paramNames.length; i++) {
                if (paramValues[i] != null && !(paramValues[i] instanceof HttpServletRequest)
                    && !(paramValues[i] instanceof HttpServletResponse)
                    && !(paramValues[i] instanceof MultipartFile)) {
                    params.put(paramNames[i], paramValues[i]);
                }
            }
        }
        return params;
    }
    
    private Map<String, Object> desensitizeParams(Map<String, Object> params) {
        Map<String, Object> desensitized = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (isSensitiveField(key)) {
                desensitized.put(key, "***");
            } else {
                desensitized.put(key, value);
            }
        }
        return desensitized;
    }
    
    private boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        String lowerName = fieldName.toLowerCase();
        return lowerName.contains("password") 
            || lowerName.contains("pwd")
            || lowerName.contains("token")
            || lowerName.contains("secret")
            || lowerName.contains("key")
            || lowerName.contains("phone")
            || lowerName.contains("mobile")
            || lowerName.contains("idcard")
            || lowerName.contains("credit");
    }
}