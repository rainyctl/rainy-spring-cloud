package cc.rainyctl.services.order.service.impl;

import cc.rainyctl.entity.Order;
import cc.rainyctl.entity.OrderItem;
import cc.rainyctl.entity.Product;
import cc.rainyctl.services.order.feign.DeductProductFeignClient;
import cc.rainyctl.services.order.feign.ProductFeignClient;
import cc.rainyctl.services.order.mapper.OrderItemMapper;
import cc.rainyctl.services.order.mapper.OrderMapper;
import cc.rainyctl.services.order.service.OrderService;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final DiscoveryClient discoveryClient;

    private final RestTemplate restTemplate;

    private final OrderMapper orderMapper;

    private final OrderItemMapper orderItemMapper;

    private final LoadBalancerClient loadBalancerClient;

    private final ProductFeignClient productFeignClient;

    private final DeductProductFeignClient deductProductFeignClient;

    @Transactional
    @SentinelResource(value = "createOrder", blockHandler = "createOrderFallback")
    @Override
    public Order createOrder(Long productId, Long userId, int count) {
        // 1. RPC call to get product info
        // Product product = getProductFromRemote(productId);
        // Product product = getProductFromRemoteWithLoadBalancing(productId);
        // Product product = getProductFromRemoteWithLoadBalancingByAnnotation(productId);
        Product product = getProductFromRemoteWithFeign(productId);

        // 2. calculate total
        BigDecimal amount = product.getPrice().multiply(new BigDecimal(count));

        // 3. save order header
        Order order = new Order();
        order.setUserId(userId);
        order.setNickName("DIO");
        order.setAddress("Cairo, Egypt");
        order.setTotalAmount(amount);
        orderMapper.insert(order);
        log.info("Order created: {}", order);

        // 4. save order item (snapshot)
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(productId);
        item.setProductName(product.getName());
        item.setProductPrice(product.getPrice());
        item.setNum(count);
        orderItemMapper.insert(item);
        log.info("OrderItem created: {}", item);

        // 5. deduct stock
        deductProductFeignClient.deductStock(productId, count);
        log.info("Product {} stock deducted by {}", productId, count);

        // 6. SIMULATE ERROR HERE
        // buy something in stock but trigger the rollback of Order here
        // you will see the stock is deducted anyway while no order is created
        if (true) {
            throw new RuntimeException("Simulated unexpected crash after remote call!");
        }

        // a better way is to separate entity from DTO
        product.setNum(count);
        product.setStock(product.getStock() - count); // as a snapshot
        order.setProductList(List.of(product));

        return order;
    }

   private Order createOrderFallback(Long productId, Long userId, int count, BlockException e) {
        Order order = new Order();
        order.setNickName("sad DIO");
        order.setAddress("JOJO not found: " + e.getClass());
        return order;
   }

    // RPC with no load balancing
    private Product getProductFromRemote(Long productId) {
        List<ServiceInstance> instances = discoveryClient.getInstances("service-product");
        ServiceInstance instance = instances.get(0);
        String url = String.format("http://%s:%s/api/product/%s", instance.getHost(), instance.getPort(), productId);
        log.info("Calling url for product: {}", url);
        return restTemplate.getForObject(url, Product.class);
    }

    // RPC with load balancing, Round-Robin fashion
    private Product getProductFromRemoteWithLoadBalancing(Long productId) {
        ServiceInstance instance = loadBalancerClient.choose("service-product");
        String url = String.format("http://%s:%s/api/product/%s", instance.getHost(), instance.getPort(), productId);
        log.info("Calling url for product: {}", url);
        return restTemplate.getForObject(url, Product.class);
    }

    // RPC with load balancing based with annotation, Round-Robin fashion
    private Product getProductFromRemoteWithLoadBalancingByAnnotation(Long productId) {
        return restTemplate.getForObject("http://service-product/api/product/" + productId, Product.class);
    }

    // RPC with load balancing with Feign
    private Product getProductFromRemoteWithFeign(Long productId) {
        Product product = productFeignClient.getProductById(productId);
        log.info("Product from Feign: {}", product);
        return product;
    }
}
