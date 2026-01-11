# Reference & Troubleshooting

## Modules

### Root Configuration

The root `pom.xml` serves as the parent project, managing:
- **Dependency Versions**: Centralizes versions for Spring Boot (3.5.9), Spring Cloud (2025.0.1), and Spring Cloud Alibaba (2025.0.0.0).
- **Bill of Materials (BOM)**: Imports `spring-cloud-dependencies` and `spring-cloud-alibaba-dependencies` for consistent dependency management across modules.

### Services

The `services` module serves as a grouping for the microservices in the system.

- **service-order**: Responsible for order processing and management.
- **service-product**: Responsible for product catalog and inventory management.

## API Reference

### Product Service (`service-product`)
Base URL: `http://localhost:9001` (or whatever port you configured)

| Method | Endpoint | Description | Parameters |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/product/hello` | Health check / Simple greeting | None |
| `GET` | `/api/product/{id}` | Get product details by ID | `id` (Path Variable): Product ID |

### Order Service (`service-order`)
Base URL: `http://localhost:8001` (or whatever port you configured)

| Method | Endpoint | Description | Parameters |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/order/create` | Create a new order | `userId` (Query): User ID<br>`productId` (Query): Product ID<br>`count` (Query, default=1): Quantity |
| `GET` | `/api/order/config` | Get configuration from Nacos | None |

## Troubleshooting

### MyBatis-Plus Dependency Issue
If you encounter `UnsatisfiedDependencyException` or bean creation errors related to `SqlSessionFactory`, check your `mybatis-plus` starter version.

**Issue:** Using `mybatis-plus-spring-boot4-starter` with Spring Boot 3.x can cause compatibility issues.

**Solution:** Use `mybatis-plus-spring-boot3-starter` (or the standard starter which auto-adapts) for Spring Boot 3 projects.

```xml
<!-- Correct dependency for Spring Boot 3 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.15</version>
</dependency>
```

### Mapper Not Found
If you see `NoSuchBeanDefinitionException` for your mappers:
1. Ensure your mapper interfaces are annotated with `@Mapper`.
2. Or use `@MapperScan("cc.rainyctl.services.*.mapper")` in your main application class.

### Nacos Config Data ID
If your configuration is not being loaded (e.g., values remain null or default):

**Issue:** You created a config with Data ID `service-order` in Nacos, but your app expects a file extension.

**Solution:** The Data ID **must include the extension**.
*   **Wrong:** `service-order`
*   **Correct:** `service-order.properties` (or `.yaml` if using YAML)

This must match what you defined in `spring.config.import`:
```properties
spring.config.import=nacos:service-order.properties
```

## Future Explorations

We are currently using Nacos in **Standalone Mode** with the embedded **Derby** database for simplicity. However, for a production-grade environment, we should explore:

1.  **Nacos with MySQL**:
    *   By default, Nacos uses Derby. Switching to MySQL ensures data persistence and allows easier management of config data.
    *   Requires executing `nacos-mysql.sql` and setting `spring.datasource.platform=mysql` in `custom.properties`.

2.  **Nacos Cluster Mode**:
    *   To ensure High Availability (HA), we should deploy a cluster of Nacos servers (3+ nodes).
    *   Requires a Load Balancer (like Nginx) in front of the cluster.
    *   Configuring `cluster.conf` with IP:Port of all nodes.

For more details, refer to the [Nacos Official Documentation](https://nacos.io/en-us/docs/cluster-mode-quick-start.html).
