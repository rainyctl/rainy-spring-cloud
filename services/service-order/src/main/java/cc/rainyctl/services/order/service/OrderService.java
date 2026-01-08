package cc.rainyctl.services.order.service;

import cc.rainyctl.common.entity.Order;

public interface OrderService {

    Order createOrder(Long productId, Long userId, int count);
}
