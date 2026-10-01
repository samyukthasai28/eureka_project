package com.ecommerce.inventory.consumer;

import com.ecommerce.common.event.InventoryReservationFailedEvent;
import com.ecommerce.common.event.InventoryReservedEvent;
import com.ecommerce.common.event.OrderItemRecord;
import com.ecommerce.common.event.OrderPlacedEvent;
import com.ecommerce.common.event.ReservedItemRecord;
import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.producer.InventoryEventProducer;
import com.ecommerce.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    @KafkaListener(topics = "${app.kafka.topics.order-placed:order-placed-topic}", groupId = "inventory-service")
    @Transactional
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        String orderNumber = event.getOrderNumber().toString();
        log.info("[INVENTORY-CONSUMER] Received OrderPlacedEvent for order: {} with {} items",
                orderNumber, event.getItems() != null ? event.getItems().size() : 0);

        List<String> shortageSkus = new ArrayList<>();
        List<Inventory> toReserve = new ArrayList<>();

        if (event.getItems() != null) {
            for (OrderItemRecord item : event.getItems()) {
                String sku = item.getSkuCode().toString();
                Optional<Inventory> invOpt = inventoryRepository.findBySkuCode(sku);
                if (invOpt.isEmpty() || !invOpt.get().canReserve(item.getQuantity())) {
                    shortageSkus.add(sku);
                } else {
                    toReserve.add(invOpt.get());
                }
            }
        }

        if (!shortageSkus.isEmpty()) {
            log.warn("[INVENTORY-CONSUMER] Stock check failed for order {}. Shortage SKUs: {}",
                    orderNumber, shortageSkus);

            List<CharSequence> shortageList = new ArrayList<>(shortageSkus);
            InventoryReservationFailedEvent failedEvent = InventoryReservationFailedEvent.newBuilder()
                    .setEventId(UUID.randomUUID().toString())
                    .setOrderId(event.getOrderId())
                    .setOrderNumber(orderNumber)
                    .setReason("Insufficient inventory stock")
                    .setShortageSkuCodes(shortageList)
                    .setTimestamp(Instant.now().toString())
                    .build();

            inventoryEventProducer.publishInventoryReservationFailed(failedEvent);
        } else {
            List<ReservedItemRecord> reservedRecords = new ArrayList<>();
            for (int i = 0; i < toReserve.size(); i++) {
                Inventory inv = toReserve.get(i);
                OrderItemRecord item = event.getItems().get(i);
                inv.reserve(item.getQuantity());
                inventoryRepository.save(inv);

                reservedRecords.add(ReservedItemRecord.newBuilder()
                        .setSkuCode(item.getSkuCode().toString())
                        .setQuantity(item.getQuantity())
                        .setPrice(item.getPrice())
                        .build());
            }

            log.info("[INVENTORY-CONSUMER] Successfully reserved inventory for order: {}", orderNumber);

            InventoryReservedEvent reservedEvent = InventoryReservedEvent.newBuilder()
                    .setEventId(UUID.randomUUID().toString())
                    .setOrderId(event.getOrderId())
                    .setOrderNumber(orderNumber)
                    .setReservedItems(reservedRecords)
                    .setStatus("RESERVED")
                    .setTimestamp(Instant.now().toString())
                    .build();

            inventoryEventProducer.publishInventoryReserved(reservedEvent);
        }
    }
}