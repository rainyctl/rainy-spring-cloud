package cc.rainyctl.services.product.service.impl;

import cc.rainyctl.common.entity.Product;
import cc.rainyctl.services.product.mapper.ProductMapper;
import cc.rainyctl.services.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Product getProductById(Long productId) {
        return productMapper.selectById(productId);
    }
}
