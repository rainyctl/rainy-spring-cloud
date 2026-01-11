# Distributed Configuration (Nacos)

## Concept
Centralizing configuration files in Nacos allows dynamic updates without restarting services.

## Implementation Steps

### 1. Dependency
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

### 2. Configure Import
`application.properties`:
```properties
spring.config.import=nacos:service-order.properties
```

### 3. Create Config in Nacos
- **Data ID**: `service-order.properties`
- **Group**: `DEFAULT_GROUP`
- **Content**: `order.timeout=300min`

### 4. Dynamic Refresh
Use `@ConfigurationProperties` for type-safe, auto-refreshing config.

```java
@Component
@ConfigurationProperties(prefix = "order")
@Data
public class OrderServiceProperties {
    private String timeout;
}
```

## Config Priority (High to Low)
1.  Command Line Args
2.  **Nacos Configuration** (Remote)
3.  Local `application.properties`

## Organization
- **Namespace**: Environments (Dev, Prod).
- **Group**: Projects/Teams (ORDER_GROUP).
- **Data ID**: Filename.
