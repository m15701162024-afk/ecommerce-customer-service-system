package com.ecommerce.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User Entity
 * 
 * Represents a user in the authentication system.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username", unique = true),
    @Index(name = "idx_email", columnList = "email", unique = true),
    @Index(name = "idx_phone", columnList = "phone", unique = true),
    @Index(name = "idx_wechat_openid", columnList = "wechat_openid", unique = true),
    @Index(name = "idx_douyin_openid", columnList = "douyin_openid", unique = true),
    @Index(name = "idx_xiaohongshu_id", columnList = "xiaohongshu_id", unique = true),
    @Index(name = "idx_ali_id", columnList = "ali_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(unique = true, length = 20)
    private String phone;
    
    @Column(name = "wechat_openid", unique = true, length = 100)
    private String wechatOpenid;
    
    @Column(name = "douyin_openid", unique = true, length = 100)
    private String douyinOpenid;
    
    @Column(name = "xiaohongshu_id", unique = true, length = 100)
    private String xiaohongshuId;
    
    @Column(name = "ali_id", unique = true, length = 100)
    private String aliId;
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "USER";
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean accountNonExpired = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean accountNonLocked = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean credentialsNonExpired = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    /**
     * Check if user is active and can authenticate
     */
    public boolean isActive() {
        return enabled && accountNonExpired && accountNonLocked && credentialsNonExpired;
    }
}