# Microservices Architecture & Operations Log

## Service Discovery & Registry Architecture (2026-10-12)
- Evaluated Eureka Server lease renewal thresholds and eviction intervals for high-availability failover.
- Verified client heartbeat timers across API Gateway, Order Service, Inventory Service, and Payment Service.
- Validated partition layout and retention periods on Kafka event brokers for `order-placed-topic` and `inventory-events-topic`.

## Circuit Breakers & Fault Tolerance Tuning (2026-10-13)
- Tuned Resilience4j sliding window size (10 calls) and failure rate threshold (50%) on API Gateway routes.
- Benchmarked fallback route handlers for `/fallback/orders`, `/fallback/inventory`, and `/fallback/payments`.
- Verified exponential backoff retry policies and bulkhead concurrency isolation under simulated degraded conditions.

## Centralized Configuration & Event Schemas (2026-10-14)
- Verified Spring Cloud Config Server native repository hierarchy and profile-specific property inheritance.
- Validated Apache Avro schema evolution and backward compatibility for `OrderPlacedEvent`, `InventoryReservedEvent`, and `PaymentProcessedEvent`.
- Audited Kafka consumer group rebalance listeners and transactional outbox table indexing in PostgreSQL.

## Observability, Tracing & Metrics Instrumentation (2026-10-15)
- Configured Micrometer Brave distributed tracing context propagation and B3 header injection across gateway routes.
- Instrumented Spring Boot Actuator Prometheus metrics scrapers for JVM garbage collection, HTTP latency, and Kafka throughput.
- Validated Kubernetes service descriptors in `k8s/` and container orchestration definitions in `docker-compose.yml`.

## End-to-End System Deployment & Operational Verification (2026-10-16)
- Executed end-to-end full stack deployment and live operational verification across all microservices.
- Launched modern executive dashboard on port 8080 with interactive topology map, instant checkout, and live Saga stepper.
- Added comprehensive system verification report in `docs/SYSTEM_VERIFICATION.md`.
