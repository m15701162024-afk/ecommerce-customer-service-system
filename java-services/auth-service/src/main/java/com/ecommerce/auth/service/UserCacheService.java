package com.ecommerce.auth.service;

import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheService {
    
    private static final String USER_CACHE_PREFIX = "user:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    private final Map<String, User> localCache = new ConcurrentHashMap<>();
    
    public Optional<User> getUserByUsername(String username) {
        if (redisTemplate != null) {
            String cacheKey = USER_CACHE_PREFIX + username;
            Object cachedUser = redisTemplate.opsForValue().get(cacheKey);
            if (cachedUser != null) {
                log.debug("User {} found in Redis cache", username);
                User user = objectMapper.convertValue(cachedUser, User.class);
                return Optional.of(user);
            }
        } else {
            User cachedUser = localCache.get(username);
            if (cachedUser != null) {
                log.debug("User {} found in local cache", username);
                return Optional.of(cachedUser);
            }
        }
        
        log.debug("User {} not in cache, fetching from database", username);
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        userOpt.ifPresent(user -> {
            cacheUser(user);
        });
        
        return userOpt;
    }
    
    public void cacheUser(User user) {
        if (redisTemplate != null) {
            String cacheKey = USER_CACHE_PREFIX + user.getUsername();
            redisTemplate.opsForValue().set(cacheKey, user, CACHE_TTL);
        } else {
            localCache.put(user.getUsername(), user);
        }
        log.debug("User {} cached", user.getUsername());
    }
    
    public void invalidateUserCache(String username) {
        if (redisTemplate != null) {
            String cacheKey = USER_CACHE_PREFIX + username;
            redisTemplate.delete(cacheKey);
        } else {
            localCache.remove(username);
        }
        log.debug("User {} cache invalidated", username);
    }
    
    public void updateUserCache(User user) {
        cacheUser(user);
    }
    
    public boolean isUserCached(String username) {
        if (redisTemplate != null) {
            String cacheKey = USER_CACHE_PREFIX + username;
            return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
        }
        return localCache.containsKey(username);
    }
}