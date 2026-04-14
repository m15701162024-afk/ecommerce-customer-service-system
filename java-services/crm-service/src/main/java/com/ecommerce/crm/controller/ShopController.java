package com.ecommerce.crm.controller;

import com.ecommerce.crm.dto.ShopCreateRequest;
import com.ecommerce.crm.dto.ShopResponse;
import com.ecommerce.crm.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Slf4j
public class ShopController {

    private final ShopService shopService;

    @GetMapping
    public ResponseEntity<List<ShopResponse>> getAllShops(
            @RequestParam(required = false) String platform) {
        
        List<ShopResponse> shops;
        if (platform != null && !platform.isEmpty()) {
            shops = shopService.getShopsByPlatform(platform);
        } else {
            shops = shopService.getAllShops();
        }
        
        return ResponseEntity.ok(shops);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopResponse> getShopById(@PathVariable Long id) {
        ShopResponse shop = shopService.getAllShops().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("店铺不存在"));
        
        return ResponseEntity.ok(shop);
    }

    @PostMapping
    public ResponseEntity<ShopResponse> createShop(@Valid @RequestBody ShopCreateRequest request) {
        log.info("Creating shop: platform={}, shopName={}", request.getPlatform(), request.getShopName());
        
        ShopResponse response = shopService.createShop(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteShop(@PathVariable Long id) {
        log.info("Deleting shop: id={}", id);
        
        shopService.deleteShop(id);
        
        return ResponseEntity.ok(Map.of("message", "店铺删除成功"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ShopResponse> updateShopStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        
        Integer status = request.get("status");
        log.info("Updating shop status: id={}, status={}", id, status);
        
        ShopResponse response = shopService.updateShopStatus(id, status);
        
        return ResponseEntity.ok(response);
    }
}