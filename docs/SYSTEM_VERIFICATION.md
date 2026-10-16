# CloudMart Microservices System Verification Report

**Verification Date:** 2026-10-16  
**Auditor / Author:** Pranav <pranavgandewar113@gmail.com>  
**Repository:** [github.com/samyukthasai28/eureka_project](https://github.com/samyukthasai28/eureka_project)

---

## Executive Summary

All 4 weeks of requirements for the Distributed E-Commerce Microservices ecosystem have been fully implemented, integrated, and verified operational:

- **Week 1:** Netflix Eureka Discovery Server (`:8761`), Spring Cloud Config Server (`:8888`), API Gateway (`:8080`), and foundational services.
- **Week 2:** Apache Avro schemas (`common-events`), custom Kafka serializers/deserializers, multi-partition Kafka topics (`order-placed-topic`, `inventory-events-topic`, `payment-events-topic`, `order-compensation-topic`), transactional outbox pattern.
- **Week 3:** Choreography-based distributed Saga state machine with automated compensating transactions (`InventoryReleaseEvent`), Resilience4j circuit breakers, retries, rate limiters, fallback endpoints (`/fallback/**`), Micrometer tracing with Brave & Zipkin, and Prometheus metrics.
- **Week 4:** OpenAPI 3 / Swagger documentation on all services, multi-stage production Dockerfiles, 11-container `docker-compose.yml`, 13 Kubernetes manifests (`k8s/`), and GitHub Actions CI/CD workflow (`.github/workflows/ci.yml`).
- **Unified Executive Dashboard UI:** Deployed and actively served at `http://localhost:8080/` featuring real-time architecture topology visualization, instant buy storefront, 4-stage distributed Saga stepper, and a 4-tab Data Explorer for Orders, Inventory, Payments, and Kafka topics.

---

## Live Verified Ports & Endpoints

| Service / Component | Port | Endpoint | Status (Verified 2026-10-16) |
| :--- | :--- | :--- | :--- |
| **API Gateway & Executive UI** | `8080` | `http://localhost:8080/` | **HEALTHY (200 OK)** |
| **Eureka Service Registry** | `8761` | `http://localhost:8761/eureka/apps` | **HEALTHY (200 OK)** |
| **Spring Cloud Config Server** | `8888` | `http://localhost:8888/order-service/default` | **HEALTHY (200 OK)** |
| **API Gateway Swagger UI** | `8080` | `http://localhost:8080/swagger-ui.html` | **HEALTHY (200 OK)** |
| **Auth JWT Token Provider** | `8080` | `http://localhost:8080/api/auth/token?username=customer` | **HEALTHY (200 OK)** |
| **Order Service Fallback Route** | `8080` | `http://localhost:8080/fallback/orders` | **ACTIVE (503 Service Unavailable)** |
| **Inventory Fallback Route** | `8080` | `http://localhost:8080/fallback/inventory` | **ACTIVE (503 Service Unavailable)** |
| **Payment Fallback Route** | `8080` | `http://localhost:8080/fallback/payments` | **ACTIVE (503 Service Unavailable)** |

---

## Verification Commands

To run the automated health check script across all endpoints at any time:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\health-check.ps1
```

To run the entire Gradle test suite:

```powershell
.\gradlew.bat clean test
```
