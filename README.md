# rainy-spring-cloud

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.1-blue)
![Spring Cloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2025.0.0.0-red)
![Nacos](https://img.shields.io/badge/Nacos-3.1.1-blue)
![Handcraft](https://img.shields.io/badge/Handcraft-99.999%25-blueviolet)
![Coffee](https://img.shields.io/badge/Coffee-15-6F4E37)

☔ Experiments with Distributed Systems in Spring Cloud.

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

## 1. Service Registry (Nacos)

### What & Why
**Concept**: A central "phonebook" where microservices list their current contact info (IP & Port).
**Importance**: In cloud environments, services scale up/down and change IPs dynamically. Hardcoding addresses is impossible.

### Action Items
1.  **Start Server**: Run Nacos (the phonebook manager).
2.  **Register Client**: Configure Spring Boot apps to tell Nacos "I am here!".

### Local Setup (Server)
- Docker (standalone):

Generate a token (any of these):

```bash
# OpenSSL (Recommended ★)
openssl rand -base64 32

# Python
python - <<'PY'
import os, base64; print(base64.b64encode(os.urandom(32)).decode())
PY

# Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

```bash
TOKEN=$(openssl rand -base64 32)
docker run --name nacos-standalone-derby \
  -e MODE=standalone \
  -e NACOS_AUTH_TOKEN=$TOKEN \
  -e NACOS_AUTH_IDENTITY_KEY=nacos \
  -e NACOS_AUTH_IDENTITY_VALUE=nacos \
  -p 8080:8080 \
  -p 8848:8848 \
  -p 9848:9848 \
  -d nacos/nacos-server:v3.1.1
```

- Binary:

```bash
sh bin/startup.sh -m standalone
# stop:
sh bin/shutdown.sh
```

- Console: http://localhost:8080
- Tested on Nacos v3.1.1
- First login credentials: username nacos, password nacos

### Wire Services to Nacos (Client)
- application.properties (or application.yml):

```properties
spring.application.name=service-order
server.port=8001
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848

# Nacos Authentication (Required if auth is enabled on server)
spring.cloud.nacos.discovery.username=nacos
spring.cloud.nacos.discovery.password=nacos
```

- Spring Boot main class:

```java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderMainApplication {
  public static void main(String[] args) {
    SpringApplication.run(OrderMainApplication.class, args);
  }
}
```

Notes:
- Ports: 8080 (console UI), 8848 (HTTP API), 9848 (gRPC channels in Nacos 2.x/3.x)
- Both services are runnable Spring Boot apps; start them to see registrations in the console

### Verification
If configured correctly, you should see logs similar to this upon startup:
```text
INFO ... [AbilityControlManager] Successfully initialize AbilityControlManager 
INFO ... [NacosServiceRegistry] nacos registry, DEFAULT_GROUP service-order 192.168.1.88:8001 register finished 
```

## 2. Service Discovery

### What & Why
**Concept**: The ability for one service to look up the "phonebook" (Registry) to find the location of another service.
**Importance**: Enables decoupling. `Service A` doesn't need to know where `Service B` is, it just asks the Registry.

### How to Use
Spring Cloud provides the `DiscoveryClient` abstraction.

**Code Example (`DiscoveryTest.java`):**

```java
@Autowired
private DiscoveryClient discoveryClient;

@Test
public void testDiscovery() {
    // Get all known service names
    for (String serviceId : discoveryClient.getServices()) {
        System.out.println("Found Service: " + serviceId);
        
        // Get specific instances for a service
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        for (ServiceInstance instance : instances) {
            System.out.println(" - " + instance.getHost() + ":" + instance.getPort());
        }
    }
}
```

### Pro Tip: Simulating Clusters in IntelliJ IDEA
You can easily simulate a cluster (multiple instances of the same service) locally:
1.  Open **Run/Debug Configurations**.
2.  Select a service (e.g., `OrderMainApplication`).
3.  Click **Copy Configuration** (or press `Ctrl+D` / `Cmd+D`).
4.  In the new configuration, add to **Program arguments**: `--server.port=8002` (or any other free port).
5.  Run both the original and the copy.
6.  Check the Nacos Console: you will see `service-order` with **2 instances**.

## 3. Remote Procedure Call (RPC)

### Architecture
We are building a system where the **Order Service** needs to fetch product details from the **Product Service**.

```mermaid
graph LR
    User((User))
    
    subgraph "Order Service (8001)"
        OrderAPI[Order API]
        OrderDB[(Order DB)]
    end
    
    subgraph "Product Service (9001)"
        ProductAPI[Product API]
        ProductDB[(Product DB)]
    end
    
    User -->|Place Order| OrderAPI
    OrderAPI -->|1. Save Order| OrderDB
    OrderAPI -->|2. Remote Call - RPC| ProductAPI
    ProductAPI -->|3. Get Product Info| ProductDB
    ProductAPI -->|4. Return Info| OrderAPI
```

### Plan
1.  **Setup Database**: Create schema for Order and Product services.
2.  **Shared Module**: Create `rainy-common` to hold shared entities (POJOs).
3.  **Implement RPC**: Use `RestTemplate` + `DiscoveryClient` (Manual Load Balancing) to call Product Service from Order Service.

### Implementation Summary (Crux)
We implemented a **manual Remote Procedure Call (RPC)** to connect the services.

**The Goal**: `Order Service` needs to talk to `Product Service` to get product details (price, name).

**The "Simple" Approach (Current Implementation)**:
Instead of using OpenFeign (not wired yet), we did it manually to understand the core concept:
1.  **Discovery**: We used `DiscoveryClient` to ask Nacos: *"Who handles 'service-product'?"*
2.  **Selection**: We blindly picked the **first available instance** (`instances.get(0)`).
3.  **Call**: We constructed a URL (`http://ip:port/product/{id}`) and fired a GET request using `RestTemplate`.

**Code Snippet (`OrderServiceImpl.java`)**:
```java
// 1. Ask Nacos for "service-product" instances
List<ServiceInstance> instances = discoveryClient.getInstances("service-product");

// 2. Pick the first one (Manual Load Balancing strategy: First Available)
ServiceInstance instance = instances.get(0);

// 3. Build URL and call
String url = String.format("http://%s:%s/api/product/%s", instance.getHost(), instance.getPort(), productId);
Product product = restTemplate.getForObject(url, Product.class);
```
*This is the foundational "glue" of microservices before adding magic like LoadBalancer or Feign.*

### Database Setup
Run the following SQL scripts to initialize your database.
**Note**: We intentionally **avoid Foreign Keys** to keep microservices decoupled and performance high. Logic is handled in the application layer.

#### Product Service Database (`rainy_product`)

```sql
CREATE DATABASE IF NOT EXISTS rainy_product;
USE rainy_product;

CREATE TABLE t_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Product ID',
    name VARCHAR(255) NOT NULL COMMENT 'Product Name',
    price DECIMAL(10, 2) NOT NULL COMMENT 'Current Price',
    stock INT NOT NULL COMMENT 'Remaining Inventory',
    INDEX idx_name (name)
) COMMENT 'Product Master Catalog';
```

#### Order Service Database (`rainy_order`)

```sql
CREATE DATABASE IF NOT EXISTS rainy_order;
USE rainy_order;

CREATE TABLE t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Order ID',
    user_id BIGINT COMMENT 'User ID',
    nick_name VARCHAR(255) COMMENT 'User Nickname',
    address VARCHAR(255) COMMENT 'Shipping Address',
    total_amount DECIMAL(10, 2) COMMENT 'Total Order Cost',
    INDEX idx_user_id (user_id)
) COMMENT 'Order Header';

CREATE TABLE t_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT 'Links to t_order.id',
    product_id BIGINT NOT NULL COMMENT 'Links to t_product.id',
    product_name VARCHAR(255) COMMENT 'Snapshot of name',
    product_price DECIMAL(10, 2) COMMENT 'Snapshot of price',
    num INT NOT NULL COMMENT 'Quantity',
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) COMMENT 'Order Items (No Foreign Keys)';
```

### Mock Data
Insert some initial data into the Product Service so we can test the RPC call later.

```sql
USE rainy_product;

INSERT INTO t_product (name, price, stock) VALUES 
('Rainy Cloud Umbrella', 99.00, 100),
('Spring Boot Mug', 25.50, 500),
('Java 17 Sticker', 5.00, 1000);
```

### Understanding Order Creation Logic
When a user places an order (e.g., `POST /api/order/create?userId=1&productId=2`), the following happens behind the scenes.
**Note**: We use `MyBatis-Plus` to simplify database interactions.

#### 1. The Logic Flow
1.  **Receive Request**: `OrderController` receives `userId` and `productId`.
2.  **Remote Call (RPC)**: `OrderService` asks `ProductService` for the price of `productId`.
3.  **Calculate Total**: Price * 1 (simple example).
4.  **Save Order**: Insert a row into `t_order`.
    *   *Magic*: MyBatis-Plus automatically fills `order.id` with the new database ID (e.g., 5001) right after insertion.
5.  **Save Item**: Insert a row into `t_order_item` using that new `order.id` to link them.

#### 2. Behind the Scenes (SQL)
Here is what the generated SQL looks like for a typical transaction:

**Step A: Get Product (RPC Call)**
*Executed by Product Service*
```sql
SELECT id, name, price, stock FROM t_product WHERE id = 2;
```

**Step B: Save Order Header**
*Executed by Order Service*
```sql
INSERT INTO t_order (user_id, nick_name, total_amount, address) 
VALUES (1, 'RainyUser', 25.50, 'Cloud City');
```
*MyBatis-Plus automatically retrieves the new `id` (e.g., `5001`).*

**Step C: Save Order Item**
*Executed by Order Service*
```sql
INSERT INTO t_order_item (order_id, product_id, product_name, product_price, num) 
VALUES (5001, 2, 'Spring Boot Mug', 25.50, 1);
```

#### 3. Code Structure (MyBatis-Plus)

**Entities**
These classes map directly to your database tables.

```java
@Data
@TableName("t_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String nickName;
    private BigDecimal totalAmount;
    
    // Not in DB table, but useful for JSON response!
    @TableField(exist = false)
    private List<Product> productList;
}
```

**Mappers**
Interfaces that handle the SQL execution. By extending `BaseMapper<T>`, you get instant CRUD powers.

**1. ProductMapper**
```java
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    // Inherited methods:
    // int insert(Product entity);
    // Product selectById(Serializable id);
    // int updateById(Product entity);
    // int deleteById(Serializable id);
    // ... and many more!
}
```

**2. OrderMapper (with Custom SQL)**
If the built-in methods aren't enough, you can write your own SQL easily.

```java
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    // Example: Custom SQL with annotation
    @Select("SELECT * FROM t_order WHERE user_id = #{userId}")
    List<Order> findByUserId(Long userId);
}
```

**3. OrderItemMapper**
```java
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    // Just empty is fine for standard usage!
}
```

#### 4. Response Example
After a successful `POST /order/create?userId=1&productId=2&count=3`, you will receive a JSON response similar to this:

```json
{ 
  "id": 14, 
  "userId": 1, 
  "nickName": "DIO", 
  "address": "Cairo, Egypt", 
  "totalAmount": 76.50, 
  "productList": [ 
    { 
      "id": 2, 
      "name": "Spring Boot Mug", 
      "price": 25.50, 
      "stock": 500,
      "num": 3 
    } 
  ] 
}
```

## 4. Load Balancing

### Why?
In a real production environment, you will likely have multiple instances of `service-product` running (e.g., on ports 9001, 9002, 9003) to handle high traffic. If `service-order` hardcodes the URL to `http://localhost:9001`, it defeats the purpose of clustering. We need a way to distribute requests across all available instances.

### Dependencies
Since Spring Cloud 2020.0, the old Netflix Ribbon has been removed. We now use **Spring Cloud LoadBalancer**.

Ensure `service-order/pom.xml` includes:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-loadbalancer</artifactId>
</dependency>
```

### Approach 1: `LoadBalancerClient` (Manual)
This is the "under-the-hood" way. You explicitly ask the Load Balancer to pick a service instance for you.

```java
@Autowired
private LoadBalancerClient loadBalancerClient;

public void callService() {
    // 1. Ask Load Balancer to choose an instance of 'service-product'
    ServiceInstance instance = loadBalancerClient.choose("service-product");
    
    // 2. Build the URL using the chosen instance's IP and Port
    String url = String.format("http://%s:%s/product/%s", instance.getHost(), instance.getPort(), 1);
    
    // 3. Make the call
    restTemplate.getForObject(url, Product.class);
}
```

### Approach 2: `@LoadBalanced` Annotation (Recommended ★)
This is the standard, most convenient way. Spring injects an interceptor into your `RestTemplate` that automatically resolves service names to IPs.

**1. Configure RestTemplate**
Add `@LoadBalanced` to your bean definition.

```java
@Configuration
public class OrderConfig {
    @Bean
    @LoadBalanced // <--- The Magic Annotation
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**2. Use Service Name in URL**
Now, instead of `localhost:9001`, you use the **Service Name** (`service-product`) registered in Nacos.

```java
// The URL is now host-agnostic!
String url = "http://service-product/api/product/" + productId;
Product product = restTemplate.getForObject(url, Product.class);
```

**How it works:**
1. Spring sees `http://service-product/api/product/...`.
2. The `LoadBalancerInterceptor` pauses the request.
3. It asks the Load Balancer: "Give me an instance for 'service-product'".
4. It rewrites the URL to `http://192.168.1.5:9001/api/product/...` and lets the request proceed.

### Client-Side vs Server-Side Load Balancing

**Client-side load balancing**
- The caller resolves service instances and picks one to call.
- Usually paired with a registry (Nacos) and a client library (Spring Cloud LoadBalancer).
- In this project: `RestTemplate` + `@LoadBalanced` calling `http://service-product/api/product/...`, or Feign with `name = "service-product"` (after enabling Feign).

**Server-side load balancing**
- The caller sends traffic to a single endpoint (VIP / gateway / DNS name).
- A load balancer routes to instances behind it (Nginx/HAProxy, cloud LB, Kubernetes Service, API Gateway).
- Typical for third-party APIs: you call `https://api.vendor.com/...` and the vendor does the balancing.

### Caching Mechanism (Important)
The client maintains a **local cache** of the service registry (the "phonebook").

1.  **First Call**: The client pulls the list of available instances from Nacos and caches it locally.
2.  **Subsequent Calls**: The client uses the **local cache** to pick an instance (Load Balancing), avoiding a network trip to Nacos for every request.
3.  **Background Updates**: The cache is periodically updated to reflect changes (e.g., new instances or crashes).
4.  **Resilience**: If Nacos goes down, your services can **still communicate** because they rely on the local cache!

```mermaid
graph TD
    subgraph OrderService ["Order Service"]
        RT[RestTemplate]
        LB[LoadBalancer]
        Cache[("Local Instance Cache")]
    end

    Nacos[("Nacos Registry")]
    
    subgraph ProductService ["Product Service Cluster"]
        P1[Instance 1]
        P2[Instance 2]
    end

    %% Flow
    RT -->|"1. Request"| LB
    LB -->|"2. Get Instances"| Cache
    Cache -.->|"3. Sync (First Time / Periodic)"| Nacos
    LB -->|"4. Select Instance"| P1
    RT -->|"5. HTTP Call"| P1

    style Cache fill:#ffcc00,stroke:#333,stroke-width:2px,color:black
```

## 5. Distributed Configuration (Nacos Config)

### What & Why
**Concept**: Centralizing configuration files (like `application.properties`) in the server (Nacos) instead of hardcoding them in each service.
**Importance**:
1.  **Dynamic Updates**: Change config on the fly without restarting the service.
2.  **Central Management**: View and manage all configs in one place.

### Implementation Status
Currently, **Service Order** is fully configured to use Nacos Config.

### How to Use

#### 1. Add Dependency
`service-order/pom.xml`:
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

#### 2. Configure Import (Spring Boot 2.4+)
`service-order/src/main/resources/application.properties`:
```properties
# Tell Spring to load config from Nacos
spring.config.import=nacos:service-order.properties

# You can also specify the Group:
# spring.config.import=nacos:service-order.properties?group=DEFAULT_GROUP
```

#### 3. Create Config in Nacos
1.  Go to Nacos Console: `http://localhost:8080/nacos` -> **Config Management** -> **Configurations**.
2.  Click **+** (Create).
3.  **Data ID**: `service-order.properties` (Must match the import above).
4.  **Group**: `DEFAULT_GROUP`.
5.  **Configuration Content**:
    ```properties
    order.timeout=300min
    order.auto-confirm=7d
    ```
    6.  Click **Publish**.

#### 4. Dynamic Refresh (Recommended: `@ConfigurationProperties`)
We expose `/config` from `OrderController` and bind Nacos config via `@ConfigurationProperties` (`OrderServiceProperties`). This keeps controllers clean and avoids `@Value` usage.

```java
@RestController
public class OrderController {
    
    // ...
}
```

#### 5. Alternative (Recommended ★): Configuration Properties
Instead of using `@Value` on every controller, you can use a Type-Safe Configuration Properties class.

**Benefit**:
- No special controller annotations needed; `@ConfigurationProperties` gives a clean, typed view of config.
- Strong typing and validation.

**1. Define Properties Class**
`OrderServiceProperties.java`:
```java
@Component
@ConfigurationProperties(prefix = "order")
@Data
public class OrderServiceProperties {
    private String timeout;
    private String autoConfirm;
}
```

**2. Inject and Use**
`OrderController.java`:
```java
@RestController
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderServiceProperties orderServiceProperties;

    @GetMapping("/config")
    public String getConfig() {
        return orderServiceProperties.toString();
    }
}
```

#### 6. Verification
1.  Call `GET http://localhost:8001/config`. You should see the values you set in Nacos.
2.  Change the values in Nacos Console and Publish.
3.  Call the endpoint again. You should see the **new values immediately** without restarting the service!

```mermaid
graph TD
    subgraph Nacos ["Nacos Server"]
        Config["Config Data<br>(service-order.properties)"]
    end
    
    subgraph Service ["Order Service"]
        Boot["Startup / Bootstrap"]
        AppEnv["Spring Environment"]
        Bean["@ConfigurationProperties Bean"]
    end
    
    Boot -->|"1. Fetch Config"| Config
    Config -->|"2. Return Properties"| Boot
    Boot -->|"3. Load into"| AppEnv
    AppEnv -->|"4. Inject Values"| Bean
    
    %% Dynamic Update Flow
    User((User/Admin)) -->|"5. Update Config"| Config
    Config -.->|"6. Push Change Event"| Service
    Service -.->|"7. Refresh Context"| Bean
    
    style Config fill:#ffcc00,stroke:#333,stroke-width:2px,color:black
```

### Advanced Configuration Topics

#### 1. Config Priorities
When you use Nacos Config, it acts as an "External Configuration" source. In Spring Boot's property resolution order:

1.  **High Priority**: Nacos Config (External)
2.  **Low Priority**: Local `application.properties` / `application.yml`

If a key exists in both, **Nacos wins**.

```mermaid
graph LR
    High["High Priority Config<br>(Nacos)"]
    Low["Low Priority Config<br>(Local)"]
    
    High --> Merge
    Low --> Merge
    
    Merge{"Merge Strategy<br>External Overrides Local"} --> Effective["Effective Config"]
    Effective --> Env["Spring Environment"]
    
    style High fill:#ff9999
    style Low fill:#99ccff
```

#### 2. Multiple Imports & Priority
You can import multiple configuration files from Nacos using `spring.config.import`.

**Priority Rule**: In a list of imports, **later imports override earlier ones**.
If `shared-database.properties` and `service-order.properties` both define `timeout`, the one listed **last** "wins".

```properties
# 1. shared-database (Base config)
# 2. service-order (Specific config - Overrides shared if collision)
spring.config.import=nacos:shared-database.properties?group=COMMON_GROUP,nacos:service-order.properties
```

#### 3. Data Organization (Namespace, Group, Data ID)
Nacos uses a hierarchical model to organize configurations, solving the problem of multi-environment and multi-service management.

*   **Namespace**: **Isolation**. Typically used for Environments (Dev, Test, Prod) or Tenants.
    *   Default: `public`.
*   **Group**: **Grouping**. Used for separating services or business units.
    *   Default: `DEFAULT_GROUP`.
*   **Data ID**: **File**. The actual configuration file for a service.
    *   Format: `service-name.properties`.

```mermaid
graph LR
    subgraph Env ["SpringBoot Environment"]
        Dev[Dev Profile]
        Prod[Prod Profile]
    end

    subgraph Nacos ["Nacos Data Model"]
        NS["**Namespace**<br>Isolation (Dev/Test/Prod)"]
        Group["**Group**<br>Category (DEFAULT_GROUP)"]
        DataID["**Data ID**<br>Config File (service.properties)"]
    end

    Dev -->|"Activates"| NS
    NS -->|"Contains"| Group
    Group -->|"Contains"| DataID
    
    style NS fill:#e1f5fe,stroke:#01579b
    style Group fill:#e8f5e9,stroke:#1b5e20
    style DataID fill:#fff3e0,stroke:#e65100
```

#### 4. Multi-Environment Support (Best Practice)
To support **Dev**, **Test**, and **Prod** environments, we use **Namespaces**.

**1. Create Namespaces in Nacos**
*   Log in to Nacos Console -> **Namespaces** -> **Create Namespace**.
*   **Name**: `Dev`, **ID**: `dev` (Manually set the ID for easier config; otherwise it's a UUID).
*   Repeat for `test` and `prod`.

**2. Create Configs**
*   Switch to `Dev` namespace. Create `service-order.properties` with dev settings.
*   Switch to `Prod` namespace. Create `service-order.properties` with prod settings.

**3. Activate in Spring Boot**
Use Spring Profiles to switch namespaces.

**application.properties** (Common):
```properties
spring.application.name=service-order
# Default profile
spring.profiles.active=dev
```

**application-dev.properties**:
```properties
# Connect to 'dev' namespace
spring.cloud.nacos.config.namespace=dev
spring.config.import=nacos:service-order.properties
```

**application-prod.properties**:
```properties
# Connect to 'prod' namespace
spring.cloud.nacos.config.namespace=prod
spring.config.import=nacos:service-order.properties
```

**4. Specifying Group and Data ID**
*   **Namespace**: `spring.cloud.nacos.config.namespace`
*   **Group**: Default is `DEFAULT_GROUP`. To change: `spring.cloud.nacos.config.group=MY_GROUP` or in import: `nacos:service-order.properties?group=MY_GROUP`.
*   **Data ID**: Explicitly defined in the import statement (`service-order.properties`).

**Alternative: Single YAML File (`application.yml`)**
Instead of multiple `.properties` files, you can use a single `.yml` file with `---` separators to define profiles.

```yaml
# Common Configuration (Base)
spring:
  application:
    name: service-order
  profiles:
    active: dev # Default profile
---
# Dev Profile Configuration
spring:
  config:
    activate:
      on-profile: dev
  cloud:
    nacos:
      config:
        namespace: dev
        group: DEFAULT_GROUP
    import:
      - nacos:service-order.properties
      - nacos:service-common.properties?group=COMMON_GROUP
---
# Prod Profile Configuration
spring:
  config:
    activate:
      on-profile: prod
  cloud:
    nacos:
      config:
        namespace: prod
    import:
      - nacos:service-order.properties
```

#### 5. Programmatic Config Listener
Sometimes you need to trigger custom logic when a configuration changes (not just updating a Bean). You can register a `Listener` using the Nacos API.

**Example (`OrderMainApplication.java`)**:
We added a listener to log changes whenever `service-order.properties` is updated.

```java
@Bean
public ApplicationRunner nacosConfigListener(NacosConfigManager nacosConfigManager) {
    return args -> {
        ConfigService configService = nacosConfigManager.getConfigService();
        configService.addListener("service-order.properties", "DEFAULT_GROUP", new Listener() {
            @Override
            public Executor getExecutor() {
                return Executors.newFixedThreadPool(4); // Async execution
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                log.info("Nacos config info changed to: {}", configInfo);
            }
        });
    };
}
```

## 6. OpenFeign (Declarative RPC)

### What & Why
**Concept**: A declarative web service client. You write an interface and annotate it; Spring Cloud generates the implementation at runtime.
**Importance**:
1.  **Cleaner Code**: Eliminates boilerplate `RestTemplate` code.
2.  **Built-in Load Balancing**: Integrates seamlessly with Spring Cloud LoadBalancer.
3.  **Type Safety**: Shared interfaces can ensure type safety between client and server.

### Implementation Status
OpenFeign is present in the project (dependency + `ProductFeignClient`), but the running code path still uses `RestTemplate` + `@LoadBalanced`. To use Feign end-to-end, enable `@EnableFeignClients` in `OrderMainApplication` and switch `OrderServiceImpl` to call `ProductFeignClient`.

### How to Use

#### 1. Add Dependency
`service-order/pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

#### 2. Enable Feign Clients
Add `@EnableFeignClients` to your main application class.

`OrderMainApplication.java`:
```java
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class OrderMainApplication { ... }
```

#### 3. Define Client Interface
Create an interface that mirrors the Controller of the service you want to call.

`ProductFeignClient.java`:
```java
@FeignClient(name = "service-product") // <--- Service Name in Nacos
public interface ProductFeignClient {
    
    @GetMapping("/product/{id}") // <--- Endpoint signature
    Product getProductById(@PathVariable("id") Long productId);
}
```

#### 4. Inject and Use
`OrderServiceImpl.java`:
```java
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final ProductFeignClient productFeignClient;

    @Override
    public Order createOrder(...) {
        // RPC call (Load Balancing is automatic!)
        Product product = productFeignClient.getProductById(productId);
        // ...
    }
}
```

#### 5. Call Third-Party APIs (Fixed Base URL)
When calling a third-party API (not registered in Nacos), you typically use a fixed base URL.

**1. Configure base URL**
`service-order/src/main/resources/application.properties`:
```properties
thirdparty.weather.base-url=https://example.com
```

**2. Define Feign client**
```java
@FeignClient(name = "thirdparty-weather", url = "${thirdparty.weather.base-url}")
public interface WeatherFeignClient {

    @PostMapping("/whapi/json/alicityweather/condition")
    String getWeather(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("token") String token,
            @RequestParam("cityId") String cityId
    );
}
```

**3. Call it**
```java
String json = weatherFeignClient.getWeather(
        "Bearer <access-token>",
        "<api-token>",
        "101010100"
);
```

Notes:
- Don’t hardcode real credentials in code or README; load them from config or a secret manager.
- With a fixed `url`, Feign is not doing service-discovery-based load balancing; any load balancing (if needed) is handled by DNS or a server-side load balancer in front of the third-party service.

#### 6. Timeout Control

By default, Feign sets conservative timeouts. In production, you must tune these to avoid cascading failures.

**Default Values (Spring Cloud OpenFeign):**
- **Connect Timeout**: 10s (Time to establish TCP connection)
- **Read Timeout**: 60s (Time to wait for response after connection)

**Configuration (`application.yml`):**
You can configure timeouts globally (`default`) or per-client (e.g., `service-product`).

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default: # Global settings
            connectTimeout: 5000 # 5s
            readTimeout: 5000    # 5s
          
          service-product: # Specific override
            connectTimeout: 1000 # 1s (Fast internal call)
            readTimeout: 2000    # 2s
```

**Timeout Flow:**

```mermaid
graph LR
    subgraph Client ["Client (Order Service)"]
        Req[Request]
        Conn{"1. Connect<br>(connectTimeout)"}
        Read{"2. Read<br>(readTimeout)"}
    end
    
    subgraph Server ["Server (Product Service)"]
        Process[Process Logic]
    end
    
    Req --> Conn
    Conn -- "Success (<1s)" --> Read
    Conn -- "Fail (>1s)" --> Timeout[SocketTimeoutException]
    
    Read --> Process
    Process --> Read
    Read -- "Success (<2s)" --> Done[Return Data]
    Read -- "Fail (>2s)" --> Timeout
    
    style Timeout fill:#ffcccc,stroke:#ff0000
    style Done fill:#ccffcc,stroke:#00ff00
```

#### 7. Retry Mechanism

**Default Behavior**:
By default, OpenFeign uses `Retryer.NEVER_RETRY`. This means if a call fails (e.g., timeout or network error), it throws an exception immediately without retrying.

**How to Enable (Not Recommended for all cases)**:
You can enable it by defining a `Retryer` bean.

```java
@Bean
public Retryer feignRetryer() {
    // period=100ms, maxPeriod=1s, maxAttempts=3
    return new Retryer.Default(100, 1000, 3);
}
```

**Common Practice: Should we use it?**

*   **Idempotency Matters**:
    *   ✅ **Safe to Retry**: Idempotent operations like `GET` (reading data) or `PUT` (updating entire resource).
    *   ❌ **Dangerous**: Non-idempotent operations like `POST` (creating orders). Retrying a timed-out `POST` request might result in duplicate orders if the server actually processed the first request but the response was lost.
*   **Recommendation**:
    *   Keep Feign's default `NEVER_RETRY`.
    *   Use **Resilience4j** (Circuit Breaker) for more granular control. It handles retries, fallbacks, and circuit breaking more robustly than Feign's simple retryer.

#### 8. Request Interceptor

**What & Why**:
Interceptors allow you to modify the request template **before** the request is sent. This is crucial for cross-cutting concerns like:
- **Authentication**: Adding `Authorization` headers (e.g., JWT tokens) to every outgoing call.
- **Logging**: Adding correlation IDs.
- **Customization**: Modifying URL parameters dynamically.

**Implementation Example**:
Create a bean that implements `RequestInterceptor`.

```java
@Bean
public RequestInterceptor requestInterceptor() {
    return template -> {
        // Add a custom header to every request
        template.header("X-Token", "SECRET-123");
        
        // Log the request
        System.out.println("Intercepted Feign Request: " + template.url());
    };
}
```

**Interception Flow**:

```mermaid
graph LR
    subgraph "Order Service"
        Feign[OpenFeign Client]
        Interceptor[Request Interceptor]
    end
    
    subgraph "Product Service"
        Controller[Product Controller]
    end
    
    Feign -->|"1. Build Request"| Interceptor
    Interceptor -->|"2. Add Header (X-Token)"| Controller
    Controller -.->|"3. Return Response"| Interceptor
    
    style Interceptor fill:#00b894,stroke:#333,color:white
    style Controller fill:#6c5ce7,stroke:#333,color:white
```

#### 9. Fallback (Preview)

**What & Why**:
When a remote service fails (timeout, error, or downtime), a **Fallback** provides a default value or alternative logic, preventing the failure from cascading to the user.

**Implementation**:
1.  Create a class implementing the Feign interface (`ProductFeignClient`).
2.  Annotate it with `@Component`.
3.  Link it in `@FeignClient`.

```java
// 1. Define Fallback
@Component
public class ProductFeignClientFallback implements ProductFeignClient {
    @Override
    public Product getProductById(Long id) {
        // Return dummy data or safe default
        return new Product(-1L, "Default Product (Service Down)", BigDecimal.ZERO, 0);
    }
}

// 2. Link in Client
@FeignClient(name = "service-product", fallback = ProductFeignClientFallback.class)
public interface ProductFeignClient { ... }
```

**⚠️ Important Note: Sentinel Integration**
Just defining a `fallback` class **won't work automatically** for circuit breaking in Spring Cloud Alibaba unless a circuit breaker implementation is present and enabled.

In this stack, **Sentinel** is the standard solution.
*   **Without Sentinel**: Fallback might only trigger on specific exceptions (like IO errors) but won't provide true circuit breaking (stop calling after N failures).
*   **With Sentinel**: We get full flow control, degradation, and circuit breaking capabilities.

*We will explore Sentinel in the next chapter to fully activate this feature.*

## 7. Sentinel (Flow Control & Reliability)

### What & Why
Sentinel is a powerful "traffic guard" for microservices. As services communicate, stability becomes critical. Sentinel protects your system from cascading failures using **Flow Control**, **Circuit Breaking**, **System Adaptive Protection**, and **Hotspot Traffic Control**.

### Architecture
Sentinel operates in a Client-Server model:
*   **Sentinel Client (Your App)**: Integrated into microservices (Web, Dubbo, OpenFeign). It reports metrics to the dashboard and pulls rules.
*   **Sentinel Dashboard**: A web console to view real-time metrics and configure rules dynamically.
*   **Rules Storage**: Rules are pushed to clients. For production, they should be persisted in Nacos/Zookeeper (Push Mode).

```mermaid
graph LR
    subgraph ControlPlane ["Control Plane"]
        Ops["User / Ops"]
        Dashboard["Sentinel Dashboard"]
    end
    
    subgraph DataPlane ["Data Plane (Microservices)"]
        App1["Order Service<br>(Sentinel Client)"]
        App2["Product Service<br>(Sentinel Client)"]
    end
    
    subgraph Persistence ["Persistence (Optional but Recommended)"]
        Nacos[("Nacos Config")]
    end
    
    Ops -->|"1. Define Rules"| Dashboard
    Dashboard -->|"2. Push Rules"| Nacos
    Nacos -.->|"3. Pull/Push Rules"| App1
    Nacos -.->|"3. Pull/Push Rules"| App2
    App1 -->|"4. Send Metrics"| Dashboard
    App2 -->|"4. Send Metrics"| Dashboard
    
    style Dashboard fill:#ff7675,color:white
    style Nacos fill:#0984e3,color:white
```

### Core Concepts: Resources & Rules

1.  **Resources**: Anything you want to protect.
    *   **Auto-Adapted**: Web APIs (`/order/create`), Dubbo methods, Feign clients.
    *   **Manual**: Code blocks wrapped in `@SentinelResource`.
2.  **Rules**: Policies applied to resources.
    *   **Flow Control (`FlowRule`)**: Limit QPS (e.g., max 10 req/s).
    *   **Degrade (`DegradeRule`)**: Circuit breaking (stop calling if error rate > 50% or RT > 500ms).
    *   **System Protection (`SystemRule`)**: Protect based on CPU/Load.
    *   **Hotspot (`ParamFlowRule`)**: Limit specific parameters (e.g., limit `productId=123`).

### Workflow

Every request goes through a chain of slots (processors).

```mermaid
graph TD
    User((Request)) --> Resource[Resource Entry]
    Resource --> Check{Sentinel Check}
    
    Check -->|Pass| Rules{Check Rules}
    
    Rules -- "Violated<br>(Flow/Degrade)" --> Block[BlockException]
    Rules -- "Passed" --> Business[Execute Business Logic]
    
    Block --> Fallback{Has Fallback?}
    Fallback -- Yes --> ExecFallback[Execute Fallback Logic]
    Fallback -- No --> Throw[Throw Exception]
    
    Business -- "Success" --> Done((End))
    Business -- "Exception" --> Fallback
    
    style Block fill:#d63031,color:white
    style ExecFallback fill:#00b894,color:white
```

### Dashboard Setup

1.  **Download**: Get `sentinel-dashboard-1.8.9.jar` (or latest) from [GitHub Releases](https://github.com/alibaba/Sentinel/releases).
2.  **Run**:
    ```bash
    java -Dserver.port=8859 -jar sentinel-dashboard-1.8.9.jar
    ```
3.  **Access**: `http://localhost:8859` (User/Pass: `sentinel` / `sentinel`).

### Quick Start: Flow Control Test

We have protected the Order creation logic with `@SentinelResource`.

**1. Code Setup (`OrderServiceImpl.java`)**:
```java
@SentinelResource(value = "createOrder") // Define resource name
@Override
public Order createOrder(...) {
    // ...
}
```

**2. Register Resource**:
Sentinel is lazy-loaded. You must trigger the endpoint once to see it in the dashboard.
```bash
curl -X POST "http://localhost:8001/order/create?userId=1&productId=1&count=1"
```

**3. Configure Rule**:
1.  Go to Dashboard -> **service-order** -> **Flow Control**.
2.  Click **+ Add Flow Rule**.
3.  **Resource Name**: `createOrder`.
4.  **QPS Threshold**: `1`.
5.  Click **Add**.

**4. Verify Blocking**:
Spam the request (more than 1 per second).

**Result**:
You will see the default blocking message:
> BlockedbySentinel(flowlimiting)

## 8. Gateway

### What & Why
The API Gateway acts as the single entry point for all clients. Instead of calling individual services (Order, Product) directly, clients send requests to the Gateway, which routes them to the appropriate service.

**Key Features**:
1.  **Unified Entry**: Single point of access for all microservices.
2.  **Request Routing**: Routes requests to the correct service based on path or other criteria.
3.  **Load Balancing**: Distributes traffic across service instances.
4.  **Traffic Control**: Rate limiting and flow control.
5.  **Identity Authentication**: Centralized auth verification.
6.  **Protocol Conversion**: Translates between protocols (e.g., HTTP to RPC).
7.  **System Monitoring**: Observability and logging.
8.  **Security Protection**: Firewall, IP whitelisting, etc.

### Architecture

```mermaid
graph LR
    Frontend[Frontend]
    Gateway[("Gateway Service<br>(Port: 7777)")]
    Registry[("Service Registry/Discovery")]
    
    subgraph Cluster ["Business Cluster"]
        Order[Order Service]
        Product[Product Service]
        Payment[Payment Service]
        Logistics[Logistics Service]
    end

    Frontend --> Gateway
    Gateway -->|"Request Routing"| Order
    Gateway -->|"Request Routing"| Product
    Gateway -->|"Request Routing"| Payment
    Gateway -->|"Request Routing"| Logistics
    
    Gateway -->|"Service Discovery"| Registry
    Order -->|"Service Registration"| Registry
    Product -->|"Service Registration"| Registry
    Payment -->|"Service Registration"| Registry
    Logistics -->|"Service Registration"| Registry
```

### Technology Choice: Reactive vs MVC

We chose **Spring Cloud Gateway (Reactive)** over the MVC variant for its superior efficiency in handling high concurrency.

*   **Reactive (WebFlux)**: Built on Project Reactor and Netty. Uses a non-blocking, event-loop model. Ideal for IO-intensive tasks like routing, allowing it to handle more concurrent requests with fewer threads.
*   **MVC (Servlet)**: Built on the traditional Servlet API. Uses a blocking, thread-per-request model.

**Important Note on Dependencies**:
As of recent Spring Cloud versions, the artifact names have become more explicit.
> `spring-cloud-starter-gateway` is deprecated. Please use `spring-cloud-starter-gateway-server-webflux` instead.

We explicitly use the WebFlux starter in `gateway/pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

**Load Balancer Dependency**:
We use `spring-cloud-starter-loadbalancer` instead of the bare `spring-cloud-loadbalancer`. The starter ensures all necessary auto-configurations are present, which is required for resolving `lb://` URIs in Gateway routes.

**Configuration Update**:
With the new `gateway-server-webflux` starter, the route configuration property has moved.
*   **Old**: `spring.cloud.gateway.routes`
*   **New**: `spring.cloud.gateway.server.webflux.routes`

### Core Concepts: Predicates & Filters

The Gateway works on a simple yet powerful flow: **Match (Predicate) -> Process (Filter) -> Route**.

*   **Route Predicate**: The logic that decides *if* a request matches a route.
    *   *Examples*: Path (`/api/**`), Method (`GET`), Header, Host, etc.
*   **Gateway Filter**: Logic applied to specific routes to modify the request or response.
    *   *Examples*: `AddRequestHeader`, `StripPrefix`, `Retry`.
    *   *Flow*: Filters act in a chain. **Pre-filters** run before sending the request to the downstream service. **Post-filters** run after receiving the response.
*   **Global Filter**: Logic applied to **all** routes.
    *   *Use Cases*: Logging, Metrics, Authentication.
*   **Customization**: You can write your own `GlobalFilter` or `GatewayFilterFactory` for custom business logic.

For a complete list of built-in predicates and filters, refer to the [Official Spring Cloud Gateway Documentation](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/).

### Cross-Origin Resource Sharing (CORS)

**Why configure it at the Gateway?**
Browsers enforce the Same-Origin Policy. When your frontend (e.g., `localhost:8080`) tries to call your API (e.g., `localhost:7777`), the browser blocks it unless CORS is configured.

Configuring CORS at the **Gateway** level is the best practice because:
1.  **Centralization**: You define the rules (allowed origins, methods, headers) in one place, rather than repeating them in every microservice (Order, Product, etc.).
2.  **Efficiency**: The Gateway handles the preflight (`OPTIONS`) requests, offloading this burden from your business services.

For configuration details, refer to the [CORS Configuration Documentation](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#cors-configuration).

## 9. Distributed Transactions (Seata)

### The Problem: Local Transactions in a Distributed World

In our current microservices architecture, each service manages its own database transaction using `@Transactional`:

*   **Order Service**: Starts a transaction, creates an order, and commits.
*   **Product Service**: Starts a transaction, deducts stock, and commits.

**The Failure Scenario**:
Imagine the following flow in `createOrder`:
1.  **Product Service** is called via Feign. It successfully deducts stock and commits its transaction.
2.  **Order Service** continues execution but encounters an error (e.g., unexpected crash or exception) *before* committing its own transaction.
3.  **Result**:
    *   Order Service **rolls back** (No Order created).
    *   Product Service **has already committed** (Stock deducted).
    *   **Data Inconsistency**: We lost inventory but gained no order.

```mermaid
sequenceDiagram
    participant Client
    participant Order as Order Service (Tx A)
    participant Product as Product Service (Tx B)
    participant DB_Order as Order DB
    participant DB_Product as Product DB

    Client->>Order: createOrder()
    Order->>Order: Start Tx A
    
    Order->>Product: deductStock() (Feign)
    Product->>Product: Start Tx B
    Product->>DB_Product: UPDATE stock = stock - 1
    Product->>Product: Commit Tx B (Permanent!)
    Product-->>Order: Success
    
    Order->>Order: ...Processing...
    Note right of Order: 💥 ERROR / EXCEPTION 💥
    
    Order->>Order: Rollback Tx A
    Note over DB_Order: No Order Created
    Note over DB_Product: Stock Deducted (Inconsistent!)
    Order-->>Client: Failure Response
```

### The Solution: Distributed Transaction Manager

To solve this, we need a mechanism to ensure that **either all** services commit **or all** roll back, maintaining data consistency across the distributed system. This is where **Seata** comes in.

#### How Seata Works

Seata uses a three-role architecture to manage distributed transactions:

1.  **TC (Transaction Coordinator)**:
    *   **Role**: The "Boss". A separate server (like Nacos) that maintains the status of the global transaction and all branch transactions.
    *   **Responsibility**: Drives the global commit or rollback.

2.  **TM (Transaction Manager)**:
    *   **Role**: The "Requester". Usually embedded in the service that starts the flow (e.g., Order Service).
    *   **Responsibility**: Defines the scope of the global transaction. It tells the TC to "Begin", "Commit", or "Rollback" the global transaction.

3.  **RM (Resource Manager)**:
    *   **Role**: The "Worker". Embedded in every service that touches a database (e.g., Order, Product, Account).
    *   **Responsibility**: Manages the local database resources. It registers its branch transaction with the TC and reports its status (success/failure).

**The Workflow**:
1.  **TM** asks **TC** to begin a new Global Transaction.
2.  **TM** calls microservices (the **RMs**).
3.  Each **RM** executes its local SQL, but before committing, it registers with **TC**.
4.  If any **RM** fails, **TM** tells **TC** to rollback. **TC** then instructs all **RMs** to undo their changes.

### Mapping Seata to Our Architecture

In our `rainy-spring-cloud` project, the roles are assigned as follows:

```mermaid
graph TD
    TC[("Seata Server<br>(Transaction Coordinator)")]
    
    subgraph "Order Service"
        TM[("TM (Transaction Manager)<br>@GlobalTransactional")]
        RM_Order[("RM (Resource Manager)<br>Order DB Agent")]
    end
    
    subgraph "Product Service"
        RM_Product[("RM (Resource Manager)<br>Product DB Agent")]
    end

    TM <-->|"Begin / Commit / Rollback"| TC
    RM_Order <-->|"Register / Report"| TC
    RM_Product <-->|"Register / Report"| TC
    
    TM -->|"RPC Call (Feign)"| RM_Product
    
    classDef tc fill:#ff9900,stroke:#333,stroke-width:2px;
    classDef tm fill:#66ccff,stroke:#333,stroke-width:2px;
    classDef rm fill:#99cc99,stroke:#333,stroke-width:2px;
    
    class TC tc;
    class TM tm;
    class RM_Order,RM_Product rm;
```

*   **TC**: The standalone Seata Server we need to deploy.
*   **TM**: The `Order Service` (specifically the `createOrder` method), which initiates the global transaction.
*   **RM**: Both `Order Service` and `Product Service` act as RMs because they both interact with their respective databases (`rainy_order` and `rainy_product`).

For more details, refer to the [Official Seata Documentation](https://seata.apache.org/docs/overview/what-is-seata/).

### Setup Guide

#### Step 1: Check Version
First, check the Seata version defined in your `spring-cloud-alibaba-dependencies` (usually in the root `pom.xml` or inherited BOM).
> For this project, the Seata version is **2.5.0**.

#### Step 2: Seata Server Setup
1.  **Download**: Download the Seata Server package from the [Official Release Page](https://github.com/apache/incubator-seata/releases).
2.  **Start**: Navigate to the `bin` folder and run the startup script.
    ```bash
    ./seata-server.sh
    ```
    *   This starts the server in the **background** (nohup).
    *   **Port**: Default is `8091`.
    *   **Logs**: Check `nohup.out` for startup logs.
    *   **Note**: The old Web UI at port `7091` is no longer available in newer versions.
3.  **Stop**: To stop the server, you need to manually kill the process (e.g., `kill -9 <pid>`).

#### Step 3: Client Configuration
To enable Seata in your microservices (Order, Product, etc.):

1.  **Add Dependency**:
    Add the starter to your service's `pom.xml`:
    ```xml
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
    </dependency>
    ```

2.  **Configuration**:
    While Seata supports Nacos for configuration management (recommended for production), for simplicity we will use a local `file.conf` in each service's `resources` folder.

    **file.conf**:
    ```conf
    service {
      # Transaction service group mapping
      # Format: vgroupMapping.<TxServiceGroup> = "<SeataServerGroup>"
      vgroupMapping.default_tx_group = "default"
      
      # Only support when registry.type=file
      default.grouplist = "127.0.0.1:8091"
      
      # Degrade current not support
      enableDegrade = false
      
      # Disable Seata
      disableGlobalTransaction = false
    }
    ```

#### Step 4: Database Setup (AT Mode)
We are using **AT Mode** (Automatic Transaction), which requires an `undo_log` table in **each** microservice's database to store the before/after images of data for rollback.

1.  **Create Table**: Run the following SQL in both `rainy_order` and `rainy_product` databases.
    *   [Official undo_log SQL Script](https://seata.apache.org/docs/user/quickstart/?utm_source=chatgpt.com#step-2-create-undo_log-table)

    ```sql
    -- Standard Seata AT Mode Undo Log Table
    CREATE TABLE IF NOT EXISTS `undo_log` (
        `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
        `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
        `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
        `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
        `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
        `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
        `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
        UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';
    ```

#### Step 5: Enable Global Transaction
Finally, annotate your business entry point (Transaction Manager) with `@GlobalTransactional`.

**OrderServiceImpl.java**:
```java
    @GlobalTransactional // <--- Starts the Global Transaction (Seata)
    @Transactional       // <--- Still needed for the Local Transaction (Database)
    @SentinelResource(value = "createOrder", blockHandler = "createOrderFallback")
    @Override
    public Order createOrder(Long productId, Long userId, int count) {
        // ... business logic ...
    }
```
*   **@GlobalTransactional**: Tells Seata to begin a global transaction. If any participant fails, Seata coordinates a global rollback.
*   **@Transactional**: Ensures the local database operations (e.g., inserting the order) are part of a local transaction.
*   **@SentinelResource**: Wraps the method for flow control and circuit breaking.

### How Seata AT Mode Works

Seata AT mode is a non-intrusive distributed transaction solution. It relies on a two-phase commit protocol but optimizes it for performance by committing local transactions **early** (in Phase 1), rather than holding database locks until Phase 2.

#### Phase 1: Execute & Prepare (The Local Commit)
In this phase, Seata intercepts your business SQL execution to generate an "Undo Log" before committing the local transaction.

1.  **Parse SQL**: Seata analyzes your SQL (e.g., `UPDATE storage_tbl SET count = count - 2 ...`).
2.  **Before Image**: Queries the data **before** the update to save the original state.
3.  **Execute SQL**: Executes the business SQL updates the database.
4.  **After Image**: Queries the data **after** the update to save the new state.
5.  **Insert Undo Log**: Inserts a record into the `undo_log` table containing both images and the Global Transaction ID (XID).
6.  **Local Commit**: Commits the Business SQL and the Undo Log insertion in **one local transaction**.
7.  **Report**: Reports the branch status to the TC (Transaction Coordinator).

#### Phase 2: Global Commit or Rollback
The TC decides whether to commit or rollback based on the status of all branches.

*   **Scenario A: Global Commit (All Success)**
    1.  TC notifies RMs (Resource Managers) to **Commit**.
    2.  RMs place the task in an async queue.
    3.  **Action**: RMs simply **delete** the `undo_log` record (since the data is already committed).
    4.  *Efficiency*: This is extremely fast as no database rollback is needed.

*   **Scenario B: Global Rollback (Any Failure)**
    1.  TC notifies RMs to **Rollback**.
    2.  RMs find the corresponding `undo_log` record using XID and Branch ID.
    3.  **Validation**: Compares the current database data with the **After Image**.
        *   *Why?* To ensure no other dirty writes occurred since Phase 1.
    4.  **Restore**: Uses the **Before Image** to restore the data to its original state.
    5.  **Cleanup**: Deletes the `undo_log` record.

#### Visualizing the AT Protocol

```mermaid
graph TD
    subgraph "Phase 1: Local Execution"
        Start((Start)) --> Parse[Parse SQL]
        Parse --> BI[Query Before Image]
        BI --> Exec[Execute Business SQL]
        Exec --> AI[Query After Image]
        AI --> InsertLog[Insert Undo Log]
        InsertLog --> Commit[Local Commit]
        Commit --> Report[Report Status to TC]
    end

    subgraph "Phase 2: Global Decision"
        Report --> Decision{TC Decision}
        
        Decision -- "Global Commit" --> AsyncQueue[Async Queue]
        AsyncQueue --> DeleteLog[Delete Undo Log]
        DeleteLog --> EndSuccess((Finish))
        
        Decision -- "Global Rollback" --> FindLog[Find Undo Log]
        FindLog --> Check["Validate Data<br>(Current == After Image?)"]
        Check -- Yes --> Restore["Restore Data<br>(Use Before Image)"]
        Restore --> DeleteLogRoll[Delete Undo Log]
        DeleteLogRoll --> EndFail((Finish))
    end
    
    style Decision fill:#f9f,stroke:#333,stroke-width:4px
    style Commit fill:#9f9,stroke:#333
    style Restore fill:#f99,stroke:#333
```

### Other Seata Modes (Brief Comparison)

Besides AT Mode (default), Seata supports three other modes for different scenarios:

1.  **TCC Mode (Try-Confirm-Cancel)**
    *   **Mechanism**: You manually implement three methods: `prepare` (Try), `commit` (Confirm), and `rollback` (Cancel).
    *   **Pros**: High performance; doesn't rely on database transactions.
    *   **Cons**: Invasive (requires writing a lot of code).
    *   **Use Case**: High-concurrency scenarios or when resources are not databases (e.g., Redis).

2.  **Saga Mode**
    *   **Mechanism**: A sequence of local transactions. If one fails, compensating transactions are executed in reverse order.
    *   **Pros**: Handles long-running business processes; asynchronous.
    *   **Cons**: No isolation (dirty reads possible); complex compensation logic.
    *   **Use Case**: Legacy systems or long chains of services.

3.  **XA Mode**
    *   **Mechanism**: Standard 2PC protocol supported by the database itself.
    *   **Pros**: Strong consistency; standard.
    *   **Cons**: Blocking (locks resources until global commit); lower performance.
    *   **Use Case**: Financial systems requiring strict ACID compliance.

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
