package com.ecommerce.order.producer;

import com.ecommerce.common.event.InventoryReleaseEvent;
import com.ecommerce.common.event.OrderCancelledEvent;
import com.ecommerce.common.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.order-placed:order-placed-topic}")
    private String orderPlacedTopic;

    @Value("${app.kafka.topics.order-compensation:order-compensation-topic}")
    private String orderCompensationTopic;

    public CompletableFuture<SendResult<String, Object>> sendOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("[ORDER-PRODUCER] Dispatching OrderPlacedEvent for order: {} to topic: {}",
                event.getOrderNumber(), orderPlacedTopic);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                orderPlacedTopic,
                event.getOrderNumber().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("[ORDER-PRODUCER] Successfully dispatched OrderPlacedEvent for order {} at offset {}",
                        event.getOrderNumber(), result.getRecordMetadata().offset());
            } else {
                log.error("[ORDER-PRODUCER] Failed to dispatch OrderPlacedEvent for order {}: {}",
                        event.getOrderNumber(), ex.getMessage());
            }
        });

        return future;
    }

    public CompletableFuture<SendResult<String, Object>> sendInventoryReleaseEvent(InventoryReleaseEvent event) {
        log.info("[ORDER-PRODUCER] Dispatching InventoryReleaseEvent for order: {} to topic: {}",
                event.getOrderNumber(), orderCompensationTopic);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                orderCompensationTopic,
                event.getOrderNumber().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("[ORDER-PRODUCER] Successfully dispatched InventoryReleaseEvent for order {} at offset {}",
                        event.getOrderNumber(), result.getRecordMetadata().offset());
            } else {
                log.error("[ORDER-PRODUCER] Failed to dispatch InventoryReleaseEvent for order {}: {}",
                        event.getOrderNumber(), ex.getMessage());
            }
        });

        return future;
    }

    public CompletableFuture<SendResult<String, Object>> sendOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("[ORDER-PRODUCER] Dispatching OrderCancelledEvent for order: {} to topic: {}",
                event.getOrderNumber(), orderCompensationTopic);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                orderCompensationTopic,
                event.getOrderNumber().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("[ORDER-PRODUCER] Successfully dispatched OrderCancelledEvent for order {} at offset {}",
                        event.getOrderNumber(), result.getRecordMetadata().offset());
            } else {
                log.error("[ORDER-PRODUCER] Failed to dispatch OrderCancelledEvent for order {}: {}",
                        event.getOrderNumber(), ex.getMessage());
            }
        });

        return future;
    }
}