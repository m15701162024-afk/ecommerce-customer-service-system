package com.ecommerce.purchase.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存服务Feign客户端
 */
@FeignClient(name = "product-service", path = "/api/inventory")
public interface InventoryServiceClient {

    /**
     * 扣减库存
     *
     * @param productId 商品ID
     * @param quantity  数量
     * @param orderNo   订单编号
     * @return 是否成功
     */
    @PostMapping("/deduct/{productId}")
    Boolean deductStock(
            @PathVariable("productId") Long productId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("orderNo") String orderNo
    );

    /**
     * 检查库存是否充足
     *
     * @param productId 商品ID
     * @param quantity  需要的数量
     * @return 是否充足
     */
    @PostMapping("/check/{productId}")
    Boolean checkStock(
            @PathVariable("productId") Long productId,
            @RequestParam("quantity") Integer quantity
    );
}