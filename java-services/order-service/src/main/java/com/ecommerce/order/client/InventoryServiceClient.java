package com.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存服务Feign客户端
 * 用于检查和预留库存
 */
@FeignClient(name = "product-service", path = "/api/inventory")
public interface InventoryServiceClient {

    /**
     * 检查库存是否充足
     *
     * @param productId 商品ID
     * @param sku       SKU编码
     * @param quantity  需要的数量
     * @return 库存是否充足
     */
    @GetMapping("/check")
    Boolean checkStock(@RequestParam("productId") Long productId,
                       @RequestParam("sku") String sku,
                       @RequestParam("quantity") Integer quantity);

    /**
     * 预留库存（创建订单时使用）
     *
     * @param productId 商品ID
     * @param sku       SKU编码
     * @param quantity  需要预留的数量
     * @param orderId   订单ID（用于关联预留记录）
     * @return 预留是否成功
     */
    @PostMapping("/reserve")
    Boolean reserveStock(@RequestParam("productId") Long productId,
                         @RequestParam("sku") String sku,
                         @RequestParam("quantity") Integer quantity,
                         @RequestParam("orderId") Long orderId);

    /**
     * 扣减库存（订单支付成功时使用）
     * 将预留库存转为实际扣减
     *
     * @param productId 商品ID
     * @param sku       SKU编码
     * @param quantity  需要扣减的数量
     * @return 扣减是否成功
     */
    @PostMapping("/deduct")
    Boolean deductStock(@RequestParam("productId") Long productId,
                        @RequestParam("sku") String sku,
                        @RequestParam("quantity") Integer quantity);

    /**
     * 释放预留库存（订单取消时使用）
     *
     * @param productId 商品ID
     * @param sku       SKU编码
     * @param quantity  需要释放的数量
     * @param orderId   订单ID
     * @return 释放是否成功
     */
    @PostMapping("/release")
    Boolean releaseStock(@RequestParam("productId") Long productId,
                         @RequestParam("sku") String sku,
                         @RequestParam("quantity") Integer quantity,
                         @RequestParam("orderId") Long orderId);
}