# OpenFeign (Declarative RPC)

## Concept
**OpenFeign** makes writing web service clients easier. You define an interface, and Feign generates the implementation.

## Setup

### 1. Dependency
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### 2. Enable
```java
@EnableFeignClients
@SpringBootApplication
public class OrderMainApplication { ... }
```

### 3. Define Interface
```java
@FeignClient(name = "service-product") 
public interface ProductFeignClient {
    @GetMapping("/api/product/{id}")
    Product getProductById(@PathVariable("id") Long id);
}
```

## Features

### Timeouts
Configure in `application.properties`:
```properties
spring.cloud.openfeign.client.config.default.connect-timeout=5000
spring.cloud.openfeign.client.config.default.read-timeout=5000
```

### Retries
Default: **NEVER_RETRY**.
Recommendation: Use Sentinel/Resilience4j for retries instead of Feign's internal retryer.

### Interceptors
Modify requests before sending (e.g., Auth headers).

```java
@Bean
public RequestInterceptor requestInterceptor() {
    return template -> template.header("X-Token", "SECRET-123");
}
```

### Fallback
Provide default logic when calls fail.
Requires **Sentinel** for full circuit breaking capabilities.
