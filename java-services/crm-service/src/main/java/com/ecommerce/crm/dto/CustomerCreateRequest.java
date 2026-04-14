package com.ecommerce.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 客户创建请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {
    
    @NotBlank(message = "客户姓名不能为空")
    private String name;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String wechat;
    
    private String avatar;
    
    private String gender;
    
    private LocalDate birthday;
    
    private String address;
    
    private String province;
    
    private String city;
    
    private String district;
    
    private String source;
    
    private String externalId;
    
    private Long storeId;
    
    private String remark;
    
    private Long groupId;
    
    private Long serviceStaffId;
    
    private String serviceStaffName;
}