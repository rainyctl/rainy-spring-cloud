# Complete System Architecture

Here is the "Big Picture" of the `rainy-spring-cloud` system, integrating all 9 key components.

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

**Component Summary**:
1.  **Nacos**: The brain. Handles Service Registry (Who is where?) and Configuration (What settings?).
2.  **Sentinel**: The guard. Protects services with Flow Control and Circuit Breaking.
3.  **Seata**: The judge. Ensures Distributed Transaction consistency (All commit or All rollback).
4.  **Gateway**: The door. Unified entry point, routing, and filtering.
5.  **Order Service**: The consumer. Orchestrates business logic.
6.  **Product Service**: The provider. Manages inventory.
7.  **OpenFeign**: The phone. Makes remote calls look like local method calls.
8.  **LoadBalancer**: The traffic cop. Distributes requests among service instances.
9.  **Database**: The vault. Stores data with Seata `undo_log` support.
