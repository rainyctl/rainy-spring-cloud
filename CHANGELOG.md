# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Fixed
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
