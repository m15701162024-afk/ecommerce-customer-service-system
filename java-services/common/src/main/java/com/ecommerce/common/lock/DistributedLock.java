package com.ecommerce.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类
 * 使用Redisson实现基于Redis的分布式锁
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    private final RedissonClient redissonClient;

    /**
     * 尝试获取分布式锁
     *
     * @param key      锁的key
     * @param waitTime 等待时间(毫秒)
     * @param leaseTime 持锁时间(毫秒)，超时自动释放
     * @return 是否成功获取锁
     */
    public boolean tryLock(String key, long waitTime, long leaseTime) {
        try {
            RLock lock = redissonClient.getLock(key);
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
            
            if (acquired) {
                log.debug("成功获取分布式锁: key={}, waitTime={}ms, leaseTime={}ms", 
                    key, waitTime, leaseTime);
            } else {
                log.warn("获取分布式锁失败: key={}, waitTime={}ms", key, waitTime);
            }
            
            return acquired;
        } catch (InterruptedException e) {
            log.error("获取分布式锁被中断: key={}", key, e);
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.error("获取分布式锁异常: key={}", key, e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     *
     * @param key 锁的key
     */
    public void unlock(String key) {
        try {
            RLock lock = redissonClient.getLock(key);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("成功释放分布式锁: key={}", key);
            } else {
                log.warn("当前线程未持有锁，无法释放: key={}", key);
            }
        } catch (Exception e) {
            log.error("释放分布式锁异常: key={}", key, e);
        }
    }

    /**
     * 检查锁是否被当前线程持有
     *
     * @param key 锁的key
     * @return 是否被当前线程持有
     */
    public boolean isHeldByCurrentThread(String key) {
        try {
            RLock lock = redissonClient.getLock(key);
            return lock.isHeldByCurrentThread();
        } catch (Exception e) {
            log.error("检查锁持有状态异常: key={}", key, e);
            return false;
        }
    }

    /**
     * 检查锁是否被锁定(被任何线程持有)
     *
     * @param key 锁的key
     * @return 是否被锁定
     */
    public boolean isLocked(String key) {
        try {
            RLock lock = redissonClient.getLock(key);
            return lock.isLocked();
        } catch (Exception e) {
            log.error("检查锁状态异常: key={}", key, e);
            return false;
        }
    }

    /**
     * 强制释放锁(无论是否被当前线程持有)
     * 注意：谨慎使用，只应在特殊场景下使用
     *
     * @param key 锁的key
     */
    public void forceUnlock(String key) {
        try {
            RLock lock = redissonClient.getLock(key);
            lock.forceUnlock();
            log.warn("强制释放分布式锁: key={}", key);
        } catch (Exception e) {
            log.error("强制释放分布式锁异常: key={}", key, e);
        }
    }

    /**
     * 获取锁的剩余持有时间
     *
     * @param key 锁的key
     * @return 剩余时间(毫秒)，-1表示锁未被持有，-2表示锁不存在
     */
    public long remainTimeToLive(String key) {
        try {
            RLock lock = redissonClient.getLock(key);
            return lock.remainTimeToLive();
        } catch (Exception e) {
            log.error("获取锁剩余时间异常: key={}", key, e);
            return -2;
        }
    }
}