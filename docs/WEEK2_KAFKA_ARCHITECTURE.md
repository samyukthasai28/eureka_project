# Week 2 Architecture Documentation: Event-Driven Kafka Microservices

**Contributor:** Pranav (`pranavgandewar113@gmail.com`)  
**Project:** Distributed E-Commerce Microservices (Event-Driven)  
**Repository:** `samyukthasai28/eureka_project`

---

## 🏛️ Event-Driven Architecture Overview

The system transitions from synchronous HTTP communication between services to an asynchronous, decoupled, event-driven architecture using **Apache Kafka**.

```mermaid
flowchart TD
    Client[Web/Mobile Client] -->|HTTP Request| Gateway[Spring Cloud API Gateway :8080]
    Gateway -->|Route /api/orders| OrderService[Order Service :8081]
    Gateway -->|Route /api/inventory| InventoryService[Inventory Service :8082]
    
    subgraph KafkaCluster [Apache Kafka Cluster]
        OrderTopic[order-placed-topic<br>3 Partitions | Key: orderNumber]
        InvTopic[inventory-events-topic<br>3 Partitions | Key: orderNumber]
    end

    OrderService -->|Publish OrderPlacedEvent| OrderTopic
    OrderTopic -->|Consume| InventoryService
    
    InventoryService -->|Verify & Reserve Stock| InvDB[(PostgreSQL / H2)]
    InventoryService -->|Publish InventoryReserved / Failed| InvTopic
    InvTopic -->|Consume| OrderService
    OrderService -->|Update Order Status CONFIRMED / REJECTED| OrderDB[(PostgreSQL / H2)]
```

---

## 🔑 Partitioning & Message Ordering Guarantees

1. **Partition Key Strategy**:
   - Both `OrderProducer` and `InventoryEventProducer` publish records with **`orderNumber` as the message key**.
   - Kafka's default murmur2 hashing ensures all events pertaining to a specific order (e.g., `ORD-A1B2C3D4`) land on the **exact same partition**.
   - Within each partition, Kafka enforces strict total FIFO order, guaranteeing that reservation responses are processed in exact chronological sequence.

2. **Topic Configurations**:
   - `order-placed-topic`: 3 Partitions, replication factor 1 (dev/local), 7-day retention.
   - `inventory-events-topic`: 3 Partitions, replication factor 1 (dev/local), 7-day retention.

3. **Producer Reliability & Idempotence**:
   - `acks = all`: Ensures write confirmation from all in-sync replicas before acknowledging.
   - `enable.idempotence = true`: Prevents duplicate records caused by producer retries at the network layer.
   - `max.in.flight.requests.per.connection = 5`: Maintains throughput while preserving strict ordering when idempotence is enabled.
   - `retries = 5`: Resilient retry with backoff.

4. **Consumer Idempotency**:
   - Managed via `OrderEventStoreService` / `t_order_events` outbox table:
   - Each inbound Kafka record's `eventId` (UUID) is checked against previously recorded events to avoid duplicate state mutations.

---

## 📋 Week 1 & Week 2 Implementation Checklist (Pranav)

### Week 1: Gateway, Service Scaffolding, Persistence & APIs
- [x] **Day 1**: Initialize Spring Cloud API Gateway with Eureka client
- [x] **Day 2**: Configure API Gateway (CORS, GlobalLoggingFilter, AuthenticationHeaderFilter)
- [x] **Day 3**: Scaffold Inventory Service with Eureka client & Spring Boot 3.3
- [x] **Day 4**: Configure Inventory PostgreSQL (`Inventory` entity, `InventoryRepository`, `DatabaseSeeder`)
- [x] **Day 5**: Implement Inventory REST APIs (stock checks, reservations, release, exception handling)
- [x] **Day 6**: Configure Spring Security (stateless filters, role/internal token protection)
- [x] **Day 7**: Run service integration tests (`InventoryControllerIntegrationTest`, end-to-end flows)

### Week 2: Kafka Event-Driven Messaging & Event Store
- [x] **Day 8**: Set up Kafka producer infrastructure & common domain event models (`common-events`)
- [x] **Day 9**: Scaffold Order Service & implement `OrderProducer` for `OrderPlacedEvent`
- [x] **Day 10**: Implement `OrderPlacedEventConsumer` in Inventory Service with stock validation
- [x] **Day 11**: Implement `InventoryEventConsumer` in Order Service with lifecycle transitions
- [x] **Day 12**: Implement `OrderEvent` repository and transactional outbox audit store
- [x] **Day 13**: Add unit and integration test coverage for Kafka producers and failure callbacks
- [x] **Day 14**: Configure topic partitions (3 partitions), partition keys, and producer idempotency