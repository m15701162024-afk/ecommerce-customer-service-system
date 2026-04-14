package com.ecommerce.common.idempotent;

import java.lang.annotation.*;

/**
 * 幂等性注解
 * 
 * 用于标识接口方法需要进行幂等性控制，防止重复提交
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    
    /**
     * 幂等性键的SpEL表达式
     * 例如: "#request.orderNo" 或 "#request.userId + ':' + #request.orderId"
     */
    String key();
    
    /**
     * 过期时间（秒）
     * 默认300秒（5分钟）
     */
    int expireTime() default 300;
    
    /**
     * 提示消息
     * 当检测到重复请求时的提示信息
     */
    String message() default "请勿重复提交请求";
}