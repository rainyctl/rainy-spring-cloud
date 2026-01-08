package cc.rainyctl.services.order.controller;

import cc.rainyctl.common.entity.Order;
import cc.rainyctl.services.order.config.OrderServiceProperties;
import cc.rainyctl.services.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final OrderServiceProperties orderServiceProperties;

    @PostMapping("/order/create")
    public Order createOrder(
            @RequestParam("userId") Long userId,
            @RequestParam("productId") Long productId,
            @RequestParam(value = "count", defaultValue = "1") int count) {
        return orderService.createOrder(productId, userId, count);
    }

    // get config from nacos
    @GetMapping("/config")
    public String getConfig() {
        return orderServiceProperties.toString();
    }
}
