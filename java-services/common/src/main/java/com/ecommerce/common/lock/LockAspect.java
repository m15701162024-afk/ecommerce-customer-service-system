package com.ecommerce.common.lock;

import com.ecommerce.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 分布式锁切面
 * 处理@Lockable注解的方法
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LockAspect {

    private final DistributedLock distributedLock;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    /**
     * 环绕通知：处理带@Lockable注解的方法
     */
    @Around("@annotation(lockable)")
    public Object around(ProceedingJoinPoint joinPoint, Lockable lockable) throws Throwable {
        String lockKey = parseLockKey(joinPoint, lockable.key());
        
        log.debug("尝试获取分布式锁: key={}, waitTime={}ms, leaseTime={}ms", 
            lockKey, lockable.waitTime(), lockable.leaseTime());

        // 尝试获取锁
        boolean locked = distributedLock.tryLock(lockKey, lockable.waitTime(), lockable.leaseTime());
        
        if (!locked) {
            log.warn("获取分布式锁失败: key={}, method={}", 
                lockKey, joinPoint.getSignature().toShortString());
            throw new BusinessException(lockable.errorMessage());
        }

        try {
            log.debug("成功获取分布式锁，开始执行方法: key={}", lockKey);
            
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            return result;
        } finally {
            // 根据配置决定是否立即释放锁
            if (lockable.immediateRelease()) {
                distributedLock.unlock(lockKey);
                log.debug("方法执行完成，释放分布式锁: key={}", lockKey);
            } else {
                log.debug("方法执行完成，锁将等待leaseTime超时自动释放: key={}", lockKey);
            }
        }
    }

    /**
     * 解析锁key的SpEL表达式
     * 支持从方法参数中提取值
     */
    private String parseLockKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();
            String[] parameterNames = discoverer.getParameterNames(method);

            // 如果key不包含SpEL表达式标记(#)，直接返回
            if (!keyExpression.contains("#")) {
                return keyExpression;
            }

            // 创建SpEL上下文
            EvaluationContext context = new StandardEvaluationContext();
            
            if (parameterNames != null && args != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    context.setVariable(parameterNames[i], args[i]);
                }
            }

            // 解析表达式
            Expression expression = parser.parseExpression(keyExpression);
            String lockKey = expression.getValue(context, String.class);
            
            if (lockKey == null || lockKey.isEmpty()) {
                log.warn("解析锁key失败，使用原始表达式: expression={}", keyExpression);
                return keyExpression;
            }

            return lockKey;
        } catch (Exception e) {
            log.error("解析锁key异常: expression={}, error={}", keyExpression, e.getMessage());
            // 解析失败时返回原始表达式
            return keyExpression;
        }
    }
}