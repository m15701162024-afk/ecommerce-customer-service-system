package com.ecommerce.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.entity.Shop;
import com.ecommerce.common.repository.ShopRepository;
import com.ecommerce.crm.dto.ShopCreateRequest;
import com.ecommerce.crm.dto.ShopResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopService {

    private final ShopRepository shopRepository;

    public List<ShopResponse> getAllShops() {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getDeleted, 0)
               .orderByDesc(Shop::getCreatedAt);
        
        List<Shop> shops = shopRepository.selectList(wrapper);
        return shops.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ShopResponse> getShopsByPlatform(String platform) {
        List<Shop> shops = shopRepository.findByPlatformAndStatus(platform, 1);
        return shops.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShopResponse createShop(ShopCreateRequest request) {
        Shop existingShop = shopRepository.findByPlatformAndPlatformShopId(
                request.getPlatform(), 
                request.getPlatformShopId()
        );
        
        if (existingShop != null) {
            throw new IllegalArgumentException("该平台店铺已存在");
        }

        Shop shop = new Shop();
        shop.setShopName(request.getShopName());
        shop.setPlatform(request.getPlatform());
        shop.setPlatformShopId(request.getPlatformShopId());
        shop.setOwnerId(request.getOwnerId());
        shop.setStatus(1);
        shop.setAccessToken(request.getAccessToken());
        shop.setRefreshToken(request.getRefreshToken());
        
        shopRepository.insert(shop);
        
        log.info("Created shop: id={}, platform={}, shopName={}", 
                shop.getId(), shop.getPlatform(), shop.getShopName());
        
        return convertToResponse(shop);
    }

    @Transactional
    public void deleteShop(Long id) {
        Shop shop = shopRepository.selectById(id);
        if (shop == null) {
            throw new IllegalArgumentException("店铺不存在");
        }
        
        shopRepository.deleteById(id);
        
        log.info("Deleted shop: id={}, platform={}, shopName={}", 
                id, shop.getPlatform(), shop.getShopName());
    }

    @Transactional
    public ShopResponse updateShopStatus(Long id, Integer status) {
        Shop shop = shopRepository.selectById(id);
        if (shop == null) {
            throw new IllegalArgumentException("店铺不存在");
        }
        
        shop.setStatus(status);
        shopRepository.updateById(shop);
        
        log.info("Updated shop status: id={}, status={}", id, status);
        
        return convertToResponse(shop);
    }

    private ShopResponse convertToResponse(Shop shop) {
        ShopResponse response = new ShopResponse();
        response.setId(shop.getId());
        response.setShopName(shop.getShopName());
        response.setPlatform(shop.getPlatform());
        response.setPlatformShopId(shop.getPlatformShopId());
        response.setOwnerId(shop.getOwnerId());
        response.setStatus(shop.getStatus());
        response.setTokenExpireTime(shop.getTokenExpireTime());
        response.setCreatedAt(shop.getCreatedAt());
        response.setUpdatedAt(shop.getUpdatedAt());
        return response;
    }
}