# Getting Started

## Prerequisites

Ensure you have the following ready:
1.  **JDK 17+**
2.  **Nacos Server** running at `127.0.0.1:8848`.
3.  **MySQL** with schemas `rainy_product` and `rainy_order`.

## Quick Start (Local)

```bash
# Start service-product (default: 9001)
./mvnw -pl services/service-product spring-boot:run

# Start service-order (default: 8001)
./mvnw -pl services/service-order spring-boot:run
```

### Smoke Checks

Once services are up:

```bash
# Check Product Service
curl http://localhost:9001/api/product/hello
curl http://localhost:9001/api/product/1

# Create an Order (Triggers RPC)
curl -X POST "http://localhost:8001/api/order/create?userId=1&productId=1&count=1"

# Check Nacos Config
curl http://localhost:8001/api/order/config
```

## API Reference

### Product Service (`service-product`)
Base URL: `http://localhost:9001`

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/product/hello` | Health check / Simple greeting |
| `GET` | `/api/product/{id}` | Get product details by ID |

### Order Service (`service-order`)
Base URL: `http://localhost:8001`

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/order/create` | Create a new order |
| `GET` | `/api/order/config` | Get configuration from Nacos |
