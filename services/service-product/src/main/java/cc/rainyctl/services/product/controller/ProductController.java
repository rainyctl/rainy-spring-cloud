package cc.rainyctl.services.product.controller;

import cc.rainyctl.entity.Product;
import cc.rainyctl.services.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable("id") Long productId) {
        log.info("Get product by id: {}", productId);
        return productService.getProductById(productId);
    }
}
