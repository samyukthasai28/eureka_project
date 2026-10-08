package com.ecommerce.inventory.consumer;

import com.ecommerce.common.event.InventoryReservationFailedEvent;
import com.ecommerce.common.event.InventoryReservedEvent;
import com.ecommerce.common.event.OrderItemDto;
import com.ecommerce.common.event.OrderPlacedEvent;
import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.producer.InventoryEventProducer;
import com.ecommerce.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderPlacedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedEventConsumer.class);

    private final InventoryRepository inventoryRepository;
    private final InventoryEventProducer inventoryEventProducer;

    @KafkaListener(topics = "${app.kafka.topics.order-placed:order-placed-topic}", groupId = "inventory-group")
    @Transactional
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("[INVENTORY-CONSUMER] Received OrderPlacedEvent for order: {} with {} items",
                event.getOrderNumber(), event.getItems() != null ? event.getItems().size() : 0);

        List<String> shortageSkus = new ArrayList<>();
        List<Inventory> toReserve = new ArrayList<>();

        if (event.getItems() != null) {
            for (OrderItemDto item : event.getItems()) {
                Optional<Inventory> invOpt = inventoryRepository.findBySkuCode(item.getSkuCode());
                if (invOpt.isEmpty() || !invOpt.get().canReserve(item.getQuantity())) {
                    shortageSkus.add(item.getSkuCode());
                } else {
                    toReserve.add(invOpt.get());
                }
            }
        }

        if (!shortageSkus.isEmpty()) {
            log.warn("[INVENTORY-CONSUMER] Stock check failed for order {}. Shortage SKUs: {}",
                    event.getOrderNumber(), shortageSkus);

            InventoryReservationFailedEvent failedEvent = InventoryReservationFailedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .reason("Insufficient inventory stock")
                    .shortageSkuCodes(shortageSkus)
                    .timestamp(LocalDateTime.now())
                    .build();

            inventoryEventProducer.publishInventoryReservationFailed(failedEvent);
        } else {
            for (int i = 0; i < toReserve.size(); i++) {
                Inventory inv = toReserve.get(i);
                OrderItemDto item = event.getItems().get(i);
                inv.reserve(item.getQuantity());
                inventoryRepository.save(inv);
            }

            log.info("[INVENTORY-CONSUMER] Successfully reserved inventory for order: {}", event.getOrderNumber());

            InventoryReservedEvent reservedEvent = InventoryReservedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .reservedItems(event.getItems())
                    .status("RESERVED")
                    .timestamp(LocalDateTime.now())
                    .build();

            inventoryEventProducer.publishInventoryReserved(reservedEvent);
        }
    }
}