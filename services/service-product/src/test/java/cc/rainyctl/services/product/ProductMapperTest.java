package cc.rainyctl.services.product;

import cc.rainyctl.services.product.mapper.ProductMapper;
import cc.rainyctl.common.entity.Product;
import cc.rainyctl.services.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductService productService;

    @Test
    public void testSelectById() {
        Product product = productMapper.selectById(1L);
        System.out.println(product);
    }

    @Test
    public void testService() {
        Product product = productService.getProductById(2L);
        System.out.println(product);
    }
}
