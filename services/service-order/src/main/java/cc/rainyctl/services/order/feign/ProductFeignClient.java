package cc.rainyctl.services.order.feign;

import cc.rainyctl.entity.Product;
import cc.rainyctl.services.order.feign.fallback.ProductFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-product", fallback = ProductFeignClientFallback.class)
public interface ProductFeignClient {
    @GetMapping("/product/{id}")
    Product getProductById(@PathVariable("id") Long productId);
}
