package com.ecommerce.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShopCreateRequest {

    @NotBlank(message = "店铺名称不能为空")
    private String shopName;

    @NotBlank(message = "平台不能为空")
    private String platform;

    @NotBlank(message = "平台店铺ID不能为空")
    private String platformShopId;

    @NotNull(message = "店主ID不能为空")
    private Long ownerId;

    private String accessToken;

    private String refreshToken;
}