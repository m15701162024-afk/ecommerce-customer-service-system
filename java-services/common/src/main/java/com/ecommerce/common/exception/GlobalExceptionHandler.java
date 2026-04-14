package com.ecommerce.common.exception;

import com.ecommerce.common.result.Result;
import com.ecommerce.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.failed(e.getCode(), e.getMessage());
    }
    
    /**
     * 参数校验异常 - @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.failed(ResultCode.VALIDATE_FAILED.getCode(), message);
    }
    
    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return Result.failed(ResultCode.VALIDATE_FAILED.getCode(), message);
    }
    
    /**
     * 参数校验异常 - @Validated
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.failed(ResultCode.VALIDATE_FAILED.getCode(), message);
    }
    
    /**
     * 缺少请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return Result.failed("缺少请求参数: " + e.getParameterName());
    }
    
    /**
     * 参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: {} - {}", e.getName(), e.getValue());
        return Result.failed("参数类型不匹配: " + e.getName());
    }
    
    /**
     * 请求方法不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMethod());
        return Result.failed(ResultCode.METHOD_NOT_ALLOWED);
    }
    
    /**
     * 404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("资源不存在: {}", e.getRequestURL());
        return Result.failed(ResultCode.NOT_FOUND);
    }
    
    /**
     * 未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        // 详细错误信息仅记录在日志中，不返回给客户端
        log.error("系统异常: {} - 异常类型: {}, 请求参数: {}", 
            request.getRequestURI(), 
            e.getClass().getName(),
            sanitizeParams(request.getParameterMap()),
            e);
        // 返回通用错误信息，防止敏感信息泄露
        return Result.failed(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "系统繁忙，请稍后重试");
    }
    
    /**
     * 脱敏处理请求参数
     */
    private String sanitizeParams(java.util.Map<String, String[]> params) {
        if (params == null || params.isEmpty()) {
            return "无";
        }
        
        return params.entrySet().stream()
            .map(entry -> {
                String key = entry.getKey();
                String[] values = entry.getValue();
                if (isSensitiveField(key)) {
                    return key + "=[***]";
                }
                return key + "=" + java.util.Arrays.toString(values);
            })
            .collect(Collectors.joining(", "));
    }
    
    /**
     * 判断是否为敏感字段
     */
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
    
    /**
     * 脱敏处理字符串（用于日志记录）
     */
    public static String desensitize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        // 脱敏手机号：保留前3后4位
        message = message.replaceAll("(1[3-9]\\d)\\d{4}(\\d{4})", "$1****$2");
        
        // 脱敏身份证号：保留前4后4位
        message = message.replaceAll("(\\d{4})\\d{10}(\\d{4})", "$1**********$2");
        
        // 脱敏银行卡号：保留前4后4位
        message = message.replaceAll("(\\d{4})\\d+(\\d{4})", "$1****$2");
        
        // 脱敏密码字段
        message = message.replaceAll("(password|pwd|passwd)\\s*[=:]\\s*\\S+", "$1=***");
        
        // 脱敏token
        message = message.replaceAll("(token|access_token|auth_token)\\s*[=:]\\s*\\S+", "$1=***");
        
        return message;
    }
}