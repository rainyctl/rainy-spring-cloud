package cc.rainyctl.services.product.service.impl;

import cc.rainyctl.entity.Product;
import cc.rainyctl.services.product.mapper.ProductMapper;
import cc.rainyctl.services.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Product getProductById(Long productId) {
        return productMapper.selectById(productId);
    }

    @Override
    public void deductStock(Long productId, int count) {
        if (count < 0) {
            throw new RuntimeException("Count must be greater than 0.");
        }
        int updated = productMapper.deductStock(productId, count);
        if (updated == 0) {
            throw new RuntimeException("Product stock is not enough.");
        }
        log.info("Product {} stock deducted by {}", productId, count);
    }
}
