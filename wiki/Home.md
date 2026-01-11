# Welcome to rainy-spring-cloud

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.1-blue)
![Spring Cloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2025.0.0.0-red)
![Nacos](https://img.shields.io/badge/Nacos-3.1.1-blue)
![Seata](https://img.shields.io/badge/Seata-2.5.0-orange)

☔ **Experiments with Distributed Systems in Spring Cloud.**

This project demonstrates a complete microservices architecture using the latest Spring Cloud Alibaba stack.

## Architecture Overview

```mermaid
graph TD
    User((User))
    
    subgraph "Control Plane"
        Nacos[("Nacos<br>(Registry & Config)<br>:8848")]
        Sentinel[("Sentinel Dashboard<br>(Monitoring)<br>:8859")]
        Seata[("Seata Server<br>(Tx Coordinator)<br>:8091")]
    end
    
    subgraph "Data Plane"
        Gateway[("API Gateway<br>:7777")]
        
        subgraph "Service Cluster"
            Order[("Order Service<br>:8001")]
            Product[("Product Service<br>:9001")]
        end
        
        subgraph "Storage"
            DB_Order[(Order DB)]
            DB_Product[(Product DB)]
        end
    end

    %% User Flow
    User -->|1. Request| Gateway
    Gateway -->|2. Route & Filter| Order
    Gateway -->|2. Route & Filter| Product
    
    %% Service Interactions
    Order -->|3. Feign RPC| Product
    
    %% Infrastructure Connections
    Gateway -.->|Register/Discover| Nacos
    Order -.->|Register/Discover/Config| Nacos
    Product -.->|Register/Discover/Config| Nacos
    
    Order -.->|Metrics/Rules| Sentinel
    Product -.->|Metrics/Rules| Sentinel
    
    Order <-->|Global Tx| Seata
    Product <-->|Global Tx| Seata
    
    Order -->|CRUD| DB_Order
    Product -->|CRUD| DB_Product

    classDef control fill:#fab1a0,stroke:#333,stroke-width:2px;
    classDef service fill:#74b9ff,stroke:#333,stroke-width:2px;
    classDef storage fill:#dfe6e9,stroke:#333,stroke-width:2px;
    
    class Nacos,Sentinel,Seata control;
    class Gateway,Order,Product service;
    class DB_Order,DB_Product storage;
```

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.5.9
- **Spring Cloud**: 2025.0.1
- **Spring Cloud Alibaba**: 2025.0.0.0
- **Service Discovery & Config**: Nacos
- **Resilience**: Sentinel
- **Gateway**: Spring Cloud Gateway (WebFlux)
- **Distributed Transaction**: Seata
