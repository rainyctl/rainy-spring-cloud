# Sentinel (Flow Control & Reliability)

## What & Why
Sentinel protects your system from cascading failures using **Flow Control** and **Circuit Breaking**.

## Architecture
- **Sentinel Client**: Embedded in apps. Reports metrics.
- **Sentinel Dashboard**: Web console for rules/metrics.
- **Rules Storage**: Nacos (Prod) or Memory (Local).

## Core Concepts
1.  **Resources**: Anything to protect (API endpoints, `@SentinelResource`).
2.  **Rules**:
    - **Flow Control**: Limit QPS.
    - **Degrade**: Circuit breaking (Error Rate / RT).
    - **System**: CPU/Load protection.

## Dashboard Setup
1.  Download `sentinel-dashboard.jar`.
2.  Run: `java -Dserver.port=8859 -jar sentinel-dashboard.jar`.
3.  Access: `http://localhost:8859` (sentinel/sentinel).

## Quick Test
1.  Add `@SentinelResource("createOrder")` to your method.
2.  Trigger the endpoint once to register it.
3.  Add Flow Rule in Dashboard: `QPS = 1`.
4.  Spam the request -> See `BlockedbySentinel`.
