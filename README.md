# rainy-spring-cloud

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.1-blue)
![Spring Cloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2025.0.0.0-red)
![Handcraft](https://img.shields.io/badge/Handcraft-99.999%25-blueviolet)

☔ Experiments with Distributed Systems in Spring Cloud.

## Project Structure

This is a multi-module Maven project structured as follows:

```
rainy-spring-cloud
├── services               # Container for microservices
│   ├── service-order      # Order Management Service
│   └── service-product    # Product Management Service
└── pom.xml                # Root Maven configuration
```

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.5.9
- **Spring Cloud**: 2025.0.1
- **Spring Cloud Alibaba**: 2025.0.0.0
- **Service Discovery**: Nacos

## Service Discovery (Nacos)

### What Nacos Offers
- Service registration and discovery for microservices
- Health checks and instance metadata (IP, port, weight, cluster)
- Web console for observing services and instances
- Optional configuration management via Nacos Config (not enabled in this repo yet)

### Local Setup
- Docker (standalone):

```bash
docker run -d --name nacos \
  -e MODE=standalone \
  -e NACOS_AUTH_ENABLE=false \
  -p 8848:8848 \
  -p 9848:9848 \
  nacos/nacos-server:latest
```

- Binary:

```bash
sh bin/startup.sh -m standalone
# stop:
sh bin/shutdown.sh
```

- Console: http://localhost:8848
- Default login (if auth enabled): nacos / nacos
- Use Nacos 2.x for compatibility with Spring Cloud Alibaba 2025.0.0.0

### Wire Services to Nacos
- application.yml:

```yaml
spring:
  application:
    name: service-order
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
```

- Spring Boot main class:

```java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderApplication {
  public static void main(String[] args) {
    SpringApplication.run(OrderApplication.class, args);
  }
}
```

Notes:
- Ports: 8848 (HTTP console/API), 9848 (gRPC channels in Nacos 2.x)
- Current modules are POM-only; add Spring Boot apps and configs to see registrations in the console

## Modules

### Root Configuration

The root `pom.xml` serves as the parent project, managing:
- **Dependency Versions**: Centralizes versions for Spring Boot (3.5.9), Spring Cloud (2025.0.1), and Spring Cloud Alibaba (2025.0.0.0).
- **Bill of Materials (BOM)**: Imports `spring-cloud-dependencies` and `spring-cloud-alibaba-dependencies` for consistent dependency management across modules.

### Services

The `services` module serves as a grouping for the microservices in the system.

- **service-order**: Responsible for order processing and management.
- **service-product**: Responsible for product catalog and inventory management.
