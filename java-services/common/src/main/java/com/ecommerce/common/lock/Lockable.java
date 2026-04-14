package com.ecommerce.common.lock;

import java.lang.annotation.*;

/**
 * 分布式锁注解
 * 用于方法级别的声明式分布式锁
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lockable {

    /**
     * 锁的key，支持SpEL表达式
     * 例如: "order:create:#request.userId"
     * 
     * SpEL支持的变量:
     * - #request: 方法参数名为request的对象
     * - #userId: 方法参数名为userId的值
     * - #方法名: 任意方法参数名
     */
    String key();

    /**
     * 等待获取锁的时间(毫秒)
     * 默认3000ms (3秒)
     */
    long waitTime() default 3000;

    /**
     * 锁的持有时间(毫秒)，超时自动释放
     * 默认5000ms (5秒)
     * 建议: 根据业务方法执行时间设置，避免锁过早释放
     */
    long leaseTime() default 5000;

    /**
     * 获取锁失败时的错误消息
     */
    String errorMessage() default "系统繁忙，请稍后重试";

    /**
     * 是否在方法执行完成后立即释放锁
     * true: 方法执行完立即释放(默认)
     * false: 等待leaseTime超时自动释放(适用于异步场景)
     */
    boolean immediateRelease() default true;
}