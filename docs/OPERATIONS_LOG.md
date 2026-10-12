# Microservices Architecture & Operations Log

## Service Discovery & Registry Architecture (2026-10-12)
- Evaluated Eureka Server lease renewal thresholds and eviction intervals for high-availability failover.
- Verified client heartbeat timers across API Gateway, Order Service, Inventory Service, and Payment Service.
- Validated partition layout and retention periods on Kafka event brokers for `order-placed-topic` and `inventory-events-topic`.
