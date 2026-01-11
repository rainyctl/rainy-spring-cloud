package cc.rainyctl.services.product.service;

import cc.rainyctl.entity.Product;

public interface ProductService {

    Product getProductById(Long productId);

    void deductStock(Long productId, int count);
}
