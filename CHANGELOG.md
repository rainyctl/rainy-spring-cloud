# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Fixed
- Aligned Nacos Docker command with official docs: added 8080 port and removed hardcoded token, using a generated token instead.
- Updated Nacos Docker command in `README.md` to fix startup error by providing required `NACOS_AUTH_TOKEN` and identity keys.
- Updated Nacos Docker command in `README.md` to fix authentication error by setting `NACOS_AUTH_ENABLE=false`.

### Changed
- Split "1. Service Registry" in `README.md` into two distinct sections: "1. Service Registry" and "2. Service Discovery" to better distinguish concepts.
- Added code example for `DiscoveryClient` usage in `README.md`.
- Renamed "Exploration 1" to "1. Service Registry (Nacos)" in `README.md` for a cleaner structure.
- Refactored `README.md` to include "1. Service Registry (Nacos)" section, explaining concepts and implementation.
- Updated architecture diagram in `README.md` to reflect actual ports: `service-order` (8001) and `service-product` (9001).
- Updated configuration examples and logs in `README.md` to match the new `service-order` port (8001).

### Added
- Updated `Order` entity in `README.md` to include `productList` with `@TableField(exist = false)` to match tutorial style.
- Expanded "Mappers" section in `README.md` with `ProductMapper`, `OrderItemMapper`, and custom SQL examples.
- Added "Code Structure (MyBatis-Plus)" to `README.md` with Entity and Mapper examples.
- Added "Understanding Order Creation Logic" to `README.md` explaining the flow and generated SQL.
- Added "3. Remote Procedure Call (RPC)" section to `README.md` including architecture diagram, DB schema plan, and mock data.
- Added "Pro Tip" to `README.md` for simulating service clusters in IntelliJ IDEA.
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
