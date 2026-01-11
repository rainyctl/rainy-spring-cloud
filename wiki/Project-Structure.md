# Project Structure

This is a multi-module Maven project structured as follows:

```
rainy-spring-cloud
├── gateway                 # API Gateway (Port: 7777)
├── rainy-common            # Shared entities (Order/Product/...)
├── services                # Container for microservices
│   ├── service-order       # Order Management Service
│   └── service-product     # Product Management Service
└── pom.xml                 # Root Maven configuration
```

## Modules Detail

### Root Configuration (`pom.xml`)

The root `pom.xml` serves as the parent project, managing:
- **Dependency Versions**: Centralizes versions for Spring Boot (3.5.9), Spring Cloud (2025.0.1), and Spring Cloud Alibaba (2025.0.0.0).
- **Bill of Materials (BOM)**: Imports `spring-cloud-dependencies` and `spring-cloud-alibaba-dependencies` for consistent dependency management across modules.

### Services (`services/`)

The `services` module serves as a grouping for the microservices in the system.

- **service-order**: Responsible for order processing and management.
- **service-product**: Responsible for product catalog and inventory management.
