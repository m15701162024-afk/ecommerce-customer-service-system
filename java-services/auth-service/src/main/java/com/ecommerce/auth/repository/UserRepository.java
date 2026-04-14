package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * User Repository
 * 
 * Data access layer for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by phone
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if phone exists
     */
    boolean existsByPhone(String phone);
    
    /**
     * Update last login time
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.username = :username")
    void updateLastLoginAt(@Param("username") String username, @Param("lastLoginAt") LocalDateTime lastLoginAt);
    
    /**
     * Update password
     */
    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.username = :username")
    void updatePassword(@Param("username") String username, @Param("password") String password);
    
    /**
     * Enable/disable user
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.username = :username")
    void updateEnabled(@Param("username") String username, @Param("enabled") boolean enabled);
    
    /**
     * Find user by Douyin openid
     */
    Optional<User> findByDouyinOpenid(String douyinOpenid);
    
    /**
     * Find user by Xiaohongshu ID
     */
    Optional<User> findByXiaohongshuId(String xiaohongshuId);
    
    /**
     * Find user by Ali ID (1688)
     */
    Optional<User> findByAliId(String aliId);
    
    /**
     * Check if Douyin openid exists
     */
    boolean existsByDouyinOpenid(String douyinOpenid);
    
    /**
     * Check if Xiaohongshu ID exists
     */
    boolean existsByXiaohongshuId(String xiaohongshuId);
    
    /**
     * Check if Ali ID exists
     */
    boolean existsByAliId(String aliId);
}