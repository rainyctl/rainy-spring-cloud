# Service Registry (Nacos)

## What & Why
**Concept**: A central "phonebook" where microservices list their current contact info (IP & Port).
**Importance**: In cloud environments, services scale up/down and change IPs dynamically. Hardcoding addresses is impossible.

## Action Items
1.  **Start Server**: Run Nacos (the phonebook manager).
2.  **Register Client**: Configure Spring Boot apps to tell Nacos "I am here!".

## Local Setup (Server)

### Docker (Recommended)

Generate a token first (openssl rand -base64 32).

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

### Binary
```bash
sh bin/startup.sh -m standalone
```

- **Console**: http://localhost:8080
- **Credentials**: nacos / nacos

## Wire Services to Nacos (Client)

### 1. Configuration
`application.properties`:
```properties
spring.application.name=service-order
server.port=8001
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848

# Auth
spring.cloud.nacos.discovery.username=nacos
spring.cloud.nacos.discovery.password=nacos
```

### 2. Annotation
```java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderMainApplication { ... }
```

## Verification
Startup logs should show:
```text
INFO ... [NacosServiceRegistry] nacos registry, DEFAULT_GROUP service-order ... register finished 
```
