package cc.rainyctl.services.order;

import cc.rainyctl.common.entity.Order;
import cc.rainyctl.common.entity.OrderItem;
import cc.rainyctl.services.order.mapper.OrderItemMapper;
import cc.rainyctl.services.order.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Test
    public void testOrderMapper() {
        List<Order> orders = orderMapper.selectList(null);
        System.out.println(orders);
    }

    @Test
    public void testOrderItemMapper() {
        List<OrderItem> items = orderItemMapper.selectList(null);
        System.out.println(items);
    }
}
