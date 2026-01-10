# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Documentation
- **README accuracy**:
    - Updated project structure to include `rainy-common`.
    - Pinned Nacos Docker image to `v3.1.1` (avoid `latest` drift).
    - Corrected Product Service default base URL to `9001`.
    - Corrected Order create example to use `POST /order/create`.
    - Clarified OpenFeign status (present but not wired end-to-end).
    - Aligned local cluster port example (`--server.port=8002`).
    - Added Quick Start commands and curl smoke checks.
    - Fixed code snippets to match `OrderMainApplication` and actual dependencies.
    - Aligned Nacos config notes with `@ConfigurationProperties` usage.
    - Added OpenFeign third-party API example (fixed base URL).
    - Added client-side vs server-side load balancing notes.
- **OpenFeign**: Added dedicated section (Section 6) covering:
    - Concept and benefits (Declarative vs Imperative).
    - Setup instructions (Dependency, `@EnableFeignClients`).
    - Usage example (`@FeignClient` interface).
    - Integration with Spring Cloud LoadBalancer.
    - Added third-party API call example (Weather API).
    - **Timeout Control**: Added section on `connectTimeout` and `readTimeout` configuration with Mermaid flow diagram.
    - **Retry Mechanism**: Added section on default behavior (`NEVER_RETRY`) and best practices (Idempotency risks).
    - **Request Interceptor**: Added section on modifying requests (e.g., adding headers) with Mermaid diagram.
    - **Fallback**: Added section on implementing `fallback` classes, with a note on the requirement for Sentinel.
- **Sentinel**: Added comprehensive section covering:
    - **Concept**: Traffic guard, Flow Control, Circuit Breaking.
    - **Architecture**: Client-Server model with Nacos persistence diagram.
    - **Core Concepts**: Resources (Auto/Manual) and Rules (Flow, Degrade, System).
    - **Workflow**: Request processing flow diagram (Check -> Rule -> Block/Fallback).
    - **Dashboard Setup**: Added guide to running Sentinel Dashboard (v1.8.9).
    - **Flow Control Test**: Added step-by-step guide to testing QPS limits with `@SentinelResource`.
    - **Flow Rules**: Added comprehensive section on Flow Control Modes (Direct, Related, Link) and Traffic Shaping Effects (Quick Fail, Warm Up, Queuing) with custom Mermaid diagrams.
    - **Exception Handling**: Added explanation of `SentinelWebInterceptor` (Path 1), `@SentinelResource` (Path 2), OpenFeign (Path 3), and Manual `SphU` (Path 4) flows.
- **Distributed Configuration**: Added comprehensive guide covering:
    - Basic setup with `spring-cloud-starter-alibaba-nacos-config`.
    - Dynamic refresh using `@RefreshScope` and recommended `@ConfigurationProperties` approach.
    - Config priority rules (Nacos vs Local, Import order).
    - Multi-environment support using Namespaces (Dev/Test/Prod).
    - Advanced config organization (Namespace, Group, Data ID) with Mermaid diagrams.
    - Single YAML file support for multi-profile configuration.
    - `spring.config.import` syntax for specific Groups.
    - Programmatic Nacos Config Listener example.
- **Load Balancing**: Added section covering:
    - Manual `LoadBalancerClient` usage.
    - Recommended `@LoadBalanced` annotation approach.
    - Service instance caching mechanism with Mermaid diagram.
    - Added client-side vs server-side comparison notes.
- **Service Discovery**: Added IntelliJ IDEA cluster simulation tip.
- **Order Service**: Added JSON response example for order creation.
- **General**:
    - Added "Coffee: 8" shield to `README.md`.
    - Added visual stars (★) to recommended approaches.
    - Added "Troubleshooting" section covering MyBatis-Plus dependency issues and Mapper scanning.

### Fixed
- Fixed Mermaid graph rendering issues in Sentinel Architecture section by quoting all node labels.
- Fixed Mermaid graph syntax in Sentinel Architecture section (explicit subgraph IDs).
- Aligned Nacos Docker command with official docs: added 8080 port and removed hardcoded token, using a generated token instead.
- Updated Nacos Docker command in `README.md` to fix startup error by providing required `NACOS_AUTH_TOKEN` and identity keys.
- Updated Nacos Docker command in `README.md` to fix authentication error by setting `NACOS_AUTH_ENABLE=false`.

### Added
- Added "Coffee: 3" shield to `README.md`.
- Added Nacos version badge to `README.md`.
- Added Architecture Mermaid diagram to `README.md` visualizing services and ports.
- Added Nacos client authentication configuration (`username`/`password`) to `README.md`.
- Added successful Nacos registration log example to `README.md`.
- Documented multiple ways to generate `NACOS_AUTH_TOKEN` (OpenSSL, Python, Node.js) and included inline generation in the Docker command.
- Added Nacos section to `README.md` with local setup instructions and service wiring examples.
- Added "Handcraft: 99%" badge to `README.md`.
- Created `README.md` with project structure, technology stack, and module descriptions.
- Added status badges to `README.md` for Java, Spring Boot, Spring Cloud versions, and PRs.
- Documented root `pom.xml` configuration in `README.md`.
