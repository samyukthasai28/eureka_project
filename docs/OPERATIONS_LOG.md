# Microservices Architecture & Operations Log

## Service Discovery & Registry Architecture (2026-10-12)
- Evaluated Eureka Server lease renewal thresholds and eviction intervals for high-availability failover.
- Verified client heartbeat timers across API Gateway, Order Service, Inventory Service, and Payment Service.
- Validated partition layout and retention periods on Kafka event brokers for `order-placed-topic` and `inventory-events-topic`.

## Circuit Breakers & Fault Tolerance Tuning (2026-10-13)
- Tuned Resilience4j sliding window size (10 calls) and failure rate threshold (50%) on API Gateway routes.
- Benchmarked fallback route handlers for `/fallback/orders`, `/fallback/inventory`, and `/fallback/payments`.
- Verified exponential backoff retry policies and bulkhead concurrency isolation under simulated degraded conditions.
