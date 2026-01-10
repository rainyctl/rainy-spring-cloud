package cc.rainyctl.services.order.controller;

import cc.rainyctl.entity.Order;
import cc.rainyctl.services.order.config.OrderServiceProperties;
import cc.rainyctl.services.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    private final OrderServiceProperties orderServiceProperties;

    @PostMapping("/create")
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
