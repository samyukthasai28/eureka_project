# CloudMart Distributed E-Commerce Microservices Platform - Project Report

**Project Name:** Distributed E-Commerce Microservices Platform  
**Technology:** Java 17+, Spring Boot 3, Spring Cloud, Apache Kafka, Apache Avro, PostgreSQL  
**Project Type:** Distributed Cloud Microservices Internship Project  
**Repository:** https://github.com/samyukthasai28/eureka_project  
**Generated PDF:** [PROJECT_REPORT.pdf](./PROJECT_REPORT.pdf)

---

### 1. Abstract
The Distributed E-Commerce Microservices Platform is an enterprise-grade, event-driven, distributed e-commerce backend built with Java 17+, Spring Boot 3, and Spring Cloud (2023.x). The primary objective of this project is to eliminate monolithic bottlenecks by decomposing the commerce ecosystem into decoupled, independently deployable microservices coordinated via high-throughput asynchronous messaging.

The system utilizes Apache Kafka with strictly typed Apache Avro schemas to achieve reliable, partition-ordered event communication across Order, Inventory, and Payment services. Data consistency across distributed databases is preserved using the Choreography-based Saga Pattern with automated compensating transactions, backed by an Outbox Pattern for guaranteed message delivery.

A centralized Netflix Eureka Server manages dynamic service discovery, Spring Cloud Config Server provides externalized configuration management, and Spring Cloud Gateway enforces non-blocking reactive routing with cryptographic HMAC-SHA256 JWT validation. Fault tolerance and distributed observability are achieved through Resilience4j circuit breakers, Micrometer Tracing with Zipkin, and Prometheus metrics. The system includes full containerization via Docker Compose and Kubernetes manifests.

---

### 2. Introduction
In modern e-commerce systems, high availability, loose coupling, and horizontal scalability are critical. Traditional monolithic architectures suffer from tight coupling, single points of failure, database lock contention, and high deployment risks.

This project delivers a resilient, cloud-native distributed microservices architecture where services communicate asynchronously via events rather than synchronous blocking HTTP calls. Each microservice manages its own dedicated persistence store, adhering strictly to the Database-per-Service design pattern.

Centralized API Gateway routing, token-based stateless security, automated fault isolation via circuit breakers, and end-to-end distributed tracing equip the platform to handle mission-critical, high-concurrency order placement and inventory reservation flows.

---

### 3. Objectives
1. To design and implement a distributed microservices ecosystem using Spring Boot 3 and Spring Cloud.
2. To provide centralized dynamic service discovery using Netflix Eureka Server.
3. To externalize microservice configuration using Spring Cloud Config Server.
4. To establish reactive API Gateway routing and enforce cryptographic HMAC-SHA256 JWT authentication.
5. To implement asynchronous event-driven communication using Apache Kafka and Apache Avro schemas.
6. To guarantee partition-level message ordering using aggregate identifiers (Order IDs) as partition keys.
7. To preserve distributed data consistency using the Choreography-based Saga pattern with automated compensation.
8. To ensure database isolation using PostgreSQL with dedicated databases per microservice.
9. To build resilience using Resilience4j circuit breakers, retries, rate limiters, and fallback routes.
10. To implement full-stack observability with Micrometer, Zipkin distributed tracing, and Prometheus metrics.
11. To containerize the multi-service system using multi-stage Dockerfiles, Docker Compose, and Kubernetes manifests.
12. To deliver a modern, interactive Executive Dashboard for real-time topology inspection and Saga visualization.

---

### 4. Technologies Used
| Technology | Purpose |
| :--- | :--- |
| Java 17 (OpenJDK 17) | Core programming language (records, modern syntax) |
| Spring Boot 3.3.x | Enterprise backend application framework |
| Spring Cloud (2023.x) | Microservices coordination framework |
| Netflix Eureka Server | Centralized dynamic service registry and discovery |
| Spring Cloud Config | Centralized externalized configuration management |
| Spring Cloud Gateway | Reactive, non-blocking API Gateway and request router |
| Apache Kafka | Distributed high-throughput event streaming backbone |
| Apache Avro | Compact, schema-enforced binary serialization for events |
| PostgreSQL | Relational persistence with isolated databases per service |
| Spring Data JPA / Hibernate | Object-relational mapping and repository persistence |
| Spring Security / JWT | HMAC-SHA256 token verification and stateless API protection |
| Resilience4j | Circuit breakers, retries, rate limiters, and fallback mechanisms |
| Micrometer & Zipkin | Distributed tracing and context propagation (B3/W3C) |
| Prometheus & Grafana | Metrics scraping, Actuator telemetry, and health dashboards |
| Docker & Docker Compose | Multi-stage image build and multi-container local orchestration |
| Kubernetes (K8s) | Production container orchestration, deployments, and services |
| Swagger / OpenAPI 3 | Interactive REST API documentation on all endpoints |
| Gradle | Multi-module build automation and dependency management |

---

### 5. System Modules
- **5.1 Service Registry (Eureka Server):** Runs on port `8761` as a discovery server for dynamic client load balancing.
- **5.2 Centralized Config Server:** Runs on port `8888` externalizing environments across services.
- **5.3 API Gateway & Security:** Runs on port `8080` verifying cryptographic HMAC-SHA256 JWT tokens.
- **5.4 Common Events Module:** Shared Gradle library defining Avro schemas and Kafka serializers.
- **5.5 Order Service:** Runs on port `8081` managing order persistence and outbox event publishing.
- **5.6 Inventory Service:** Runs on port `8082` managing stock reservations and Saga rollbacks.
- **5.7 Payment Service:** Runs on port `8083` recording transactions in `payment_db`.
- **5.8 Executive Dashboard:** Live storefront, architecture map, Saga visualizer at `http://localhost:8080/`.

---

### 6. System Architecture
Choreography-based Saga pattern coordinating events asynchronously across `order-placed-topic`, `inventory-events-topic`, `payment-events-topic`, and `order-compensation-topic`.

---

### 7. Reliability & Observability Features
- Choreography-Based Saga & Compensating Transactions.
- Transactional Outbox Pattern for guaranteed event delivery.
- Partition key hashing on Order ID for strict sequential ordering.
- Resilience4j Circuit Breakers, Retries, and Fallbacks.
- Brave / Zipkin distributed tracing across HTTP and Kafka.
- Actuator and Prometheus operational metrics.

---

### 8. Database & Storage
- PostgreSQL isolated databases: `order_db`, `inventory_db`, `payment_db`.
- Apache Kafka durable event log partitions.

---

### 9. API Documentation
- API Gateway: `http://localhost:8080/swagger-ui.html`
- Order Service: `http://localhost:8081/swagger-ui.html`
- Inventory Service: `http://localhost:8082/swagger-ui.html`
- Payment Service: `http://localhost:8083/swagger-ui.html`

---

### 10. Development Progress & Milestones
- **Week 1:** Discovery, Config, Gateway, JWT, PostgreSQL, Payment Foundation — **COMPLETE**
- **Week 2:** Kafka, Avro Schemas, Producer/Consumer, Partitioning, Outbox — **COMPLETE**
- **Week 3:** Saga Choreography, Compensations, Circuit Breakers, Observability — **COMPLETE**
- **Week 4:** OpenAPI 3, Dockerfiles, Docker Compose, Kubernetes, CI/CD — **COMPLETE**

---

### 11. Advantages
Scalability, fault isolation, strict ordering, contract schema safety, and end-to-end visibility.

---

### 12. Future Scope
HashiCorp Vault secret management, CQRS with Elasticsearch, Debezium CDC, and ArgoCD GitOps.

---

### 13. Conclusion
Demonstrates a production-ready, cloud-native distributed microservices architecture uniting Spring Boot 3, Kafka, Avro, PostgreSQL, and Kubernetes.
