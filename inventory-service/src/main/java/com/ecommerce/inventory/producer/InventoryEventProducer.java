package com.ecommerce.inventory.producer;

import com.ecommerce.common.event.InventoryReservationFailedEvent;
import com.ecommerce.common.event.InventoryReservedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.inventory-events:inventory-events-topic}")
    private String inventoryEventsTopic;

    public void publishInventoryReserved(InventoryReservedEvent event) {
        log.info("[KAFKA-PRODUCER] Publishing InventoryReservedEvent for order: {}", event.getOrderNumber());
        kafkaTemplate.send(inventoryEventsTopic, event.getOrderNumber(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[KAFKA-PRODUCER] Successfully published InventoryReservedEvent [offset={}]",
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("[KAFKA-PRODUCER] Failed to publish InventoryReservedEvent: {}", ex.getMessage());
                    }
                });
    }

    public void publishInventoryReservationFailed(InventoryReservationFailedEvent event) {
        log.warn("[KAFKA-PRODUCER] Publishing InventoryReservationFailedEvent for order: {}", event.getOrderNumber());
        kafkaTemplate.send(inventoryEventsTopic, event.getOrderNumber(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[KAFKA-PRODUCER] Successfully published InventoryReservationFailedEvent [offset={}]",
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("[KAFKA-PRODUCER] Failed to publish InventoryReservationFailedEvent: {}", ex.getMessage());
                    }
                });
    }
}