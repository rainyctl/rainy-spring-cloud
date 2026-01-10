package cc.rainyctl.services.order.service;

import cc.rainyctl.entity.Order;

public interface OrderService {

    Order createOrder(Long productId, Long userId, int count);
}
