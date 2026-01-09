package cc.rainyctl.services.order.feign.fallback;

import cc.rainyctl.common.entity.Product;
import cc.rainyctl.services.order.feign.ProductFeignClient;
import org.springframework.stereotype.Component;

@Component
public class ProductFeignClientFallback implements ProductFeignClient {
    @Override
    public Product getProductById(Long productId) {
        Product product = new Product();
        product.setId(-1L);
        product.setName("Product Not Found");
        return product;
    }
}
