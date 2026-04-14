package com.ecommerce.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 客户更新请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateRequest {
    
    private String name;
    
    private String phone;
    
    private String email;
    
    private String wechat;
    
    private String avatar;
    
    private String gender;
    
    private LocalDate birthday;
    
    private String address;
    
    private String province;
    
    private String city;
    
    private String district;
    
    private String remark;
    
    private Long groupId;
    
    private Long serviceStaffId;
    
    private String serviceStaffName;
    
    private Boolean enabled;
    
    private Boolean isBlacklist;
}