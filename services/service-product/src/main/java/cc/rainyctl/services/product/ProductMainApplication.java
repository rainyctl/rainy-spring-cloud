package cc.rainyctl.services.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient // enable service discovery
@MapperScan("cc.rainyctl.services.product.mapper")
@SpringBootApplication
public class ProductMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductMainApplication.class, args);
    }
}
