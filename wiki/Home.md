# rainy-spring-cloud

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.1-blue)
![Spring Cloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2025.0.0.0-red)
![Nacos](https://img.shields.io/badge/Nacos-3.1.1-blue)
![Seata](https://img.shields.io/badge/Seata-2.5.0-orange)

☔ **Experiments with Distributed Systems in Spring Cloud.**

This project demonstrates a complete microservices architecture using the latest Spring Cloud Alibaba stack.

## Architecture

```mermaid
graph TD
    subgraph Infrastructure
        Nacos[("Nacos Server<br>(Registry & Config)")]
        NacosPorts["Ports:<br>8080 (Console)<br>8848 (API)<br>9848 (gRPC)"]
        Nacos --- NacosPorts
    end

    subgraph Microservices
        Order[("Service Order<br>(Port: 8001)")]
        Product[("Service Product<br>(Port: 9001)")]
    end

    Order -->|Register & Discover| Nacos
    Product -->|Register & Discover| Nacos
    
    classDef service fill:#f9f,stroke:#333,stroke-width:2px;
    classDef infra fill:#9cf,stroke:#333,stroke-width:2px;
    
    class Order,Product service;
    class Nacos infra;
```

## Project Structure

This is a multi-module Maven project structured as follows:

```
rainy-spring-cloud
├── gateway                 # API Gateway (Port: 7777)
├── rainy-common            # Shared entities (Order/Product/...)
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

## Quick Start (Local)

Prereqs: Nacos at `127.0.0.1:8848` and MySQL schemas `rainy_product` / `rainy_order` (see “Database Setup”).

```bash
# Start service-product (default: 9001)
./mvnw -pl services/service-product spring-boot:run

# Start service-order (default: 8001)
./mvnw -pl services/service-order spring-boot:run
```

```bash
# Smoke checks
curl http://localhost:9001/api/product/hello
curl http://localhost:9001/api/product/1
curl -X POST "http://localhost:8001/api/order/create?userId=1&productId=1&count=1"
curl http://localhost:8001/api/order/config
```
