package com.ecommerce.crm.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 客户标签操作请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTagOperationRequest {
    
    @NotEmpty(message = "客户ID列表不能为空")
    private List<Long> customerIds;
    
    @NotEmpty(message = "标签ID列表不能为空")
    private List<Long> tagIds;
}