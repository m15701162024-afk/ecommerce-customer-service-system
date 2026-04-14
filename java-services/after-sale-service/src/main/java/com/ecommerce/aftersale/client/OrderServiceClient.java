package com.ecommerce.aftersale.client;

import com.ecommerce.aftersale.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单服务Feign客户端
 * 用于调用订单服务接口获取订单信息、更新订单状态
 */
@FeignClient(name = "order-service", path = "/api/orders")
public interface OrderServiceClient {

    /**
     * 根据订单ID获取订单详情
     *
     * @param id 订单ID
     * @return 订单信息
     */
    @GetMapping("/{id}")
    OrderDTO getOrderById(@PathVariable("id") Long id);

    /**
     * 根据订单编号获取订单详情
     *
     * @param orderNo 订单编号
     * @return 订单信息
     */
    @GetMapping("/no/{orderNo}")
    OrderDTO getOrderByNo(@PathVariable("orderNo") String orderNo);

    /**
     * 更新订单状态
     *
     * @param id     订单ID
     * @param status 新状态
     * @param remark 备注
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    Boolean updateOrderStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "remark", required = false) String remark
    );
}