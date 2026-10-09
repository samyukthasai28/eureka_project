package com.ecommerce.order.consumer;

import com.ecommerce.common.event.InventoryReservationFailedEvent;
import com.ecommerce.common.event.InventoryReservedEvent;
import com.ecommerce.order.service.OrderEventHandlerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = "${app.kafka.topics.inventory-events:inventory-events-topic}", groupId = "order-group")
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);
    private final OrderEventHandlerService orderEventHandlerService;

    @KafkaHandler
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("[ORDER-CONSUMER] Received InventoryReservedEvent for order: {}", event.getOrderNumber());
        orderEventHandlerService.handleInventoryReserved(event);
    }

    @KafkaHandler
    public void handleInventoryReservationFailed(InventoryReservationFailedEvent event) {
        log.warn("[ORDER-CONSUMER] Received InventoryReservationFailedEvent for order: {}", event.getOrderNumber());
        orderEventHandlerService.handleInventoryReservationFailed(event);
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object unknown) {
        log.debug("[ORDER-CONSUMER] Received unhandled event payload: {}", unknown);
    }
}