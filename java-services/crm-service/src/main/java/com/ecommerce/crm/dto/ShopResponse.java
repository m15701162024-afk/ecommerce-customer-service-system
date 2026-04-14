package com.ecommerce.crm.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShopResponse {

    private Long id;

    private String shopName;

    private String platform;

    private String platformShopId;

    private Long ownerId;

    private Integer status;

    private LocalDateTime tokenExpireTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}