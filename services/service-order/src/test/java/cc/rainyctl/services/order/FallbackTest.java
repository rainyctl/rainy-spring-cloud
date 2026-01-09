package cc.rainyctl.services.order;

import cc.rainyctl.common.entity.Product;
import cc.rainyctl.services.order.feign.ProductFeignClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FallbackTest {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Test
    public void testProductFallback() {
        Product product = productFeignClient.getProductById(999L); // doesn't exist, use fallback (require Sentinel)
        System.out.println(product);
    }
}
