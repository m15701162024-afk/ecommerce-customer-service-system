package com.ecommerce.common.idempotent;

import com.ecommerce.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final RedissonClient redissonClient;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        String keyExpression = idempotent.key();
        String key = parseKey(keyExpression, method, args);

        String redisKey = "idempotent:" + key;
        RBucket<String> bucket = redissonClient.getBucket(redisKey);

        boolean success = bucket.trySet("processing", idempotent.expireTime(), TimeUnit.SECONDS);

        if (!success) {
            log.warn("重复请求被拦截: key={}, method={}", redisKey, method.getName());
            throw new BusinessException(idempotent.message());
        }

        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            bucket.delete();
            throw throwable;
        }
    }

    private String parseKey(String keyExpression, Method method, Object[] args) {
        String[] paramNames = discoverer.getParameterNames(method);
        if (paramNames == null) {
            paramNames = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                paramNames[i] = "arg" + i;
            }
        }

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && paramNames[i] != null) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        try {
            Expression expression = parser.parseExpression(keyExpression);
            Object value = expression.getValue(context);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            log.error("解析SpEL表达式失败: expression={}, error={}", keyExpression, e.getMessage());
            throw new BusinessException("幂等性键表达式解析失败");
        }
    }
}