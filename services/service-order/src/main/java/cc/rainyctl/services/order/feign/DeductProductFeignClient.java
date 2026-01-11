package cc.rainyctl.services.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "service-product", contextId = "deductProductFeignClient")
public interface DeductProductFeignClient {
    @PostMapping("/api/product/stock/deduct")
    void deductStock(@RequestParam("productId") Long productId,
                     @RequestParam("count") int count);
}
