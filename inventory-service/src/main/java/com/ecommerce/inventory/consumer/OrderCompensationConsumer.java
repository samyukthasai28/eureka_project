package com.ecommerce.inventory.consumer;

import com.ecommerce.common.event.InventoryReleaseEvent;
import com.ecommerce.common.event.OrderCancelledEvent;
import com.ecommerce.common.event.ReleaseItemRecord;
import com.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = "${app.kafka.topics.order-compensation:order-compensation-topic}", groupId = "inventory-service")
public class OrderCompensationConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCompensationConsumer.class);
    private final InventoryService inventoryService;

    @KafkaHandler
    public void handleInventoryRelease(InventoryReleaseEvent event) {
        String orderNumber = event.getOrderNumber().toString();
        log.warn("[SAGA-COMPENSATION] Received InventoryReleaseEvent for order: {}, reason: {}",
                orderNumber, event.getReason());

        if (event.getItems() != null) {
            for (ReleaseItemRecord item : event.getItems()) {
                String sku = item.getSkuCode().toString();
                int qty = item.getQuantity();
                try {
                    inventoryService.releaseStock(sku, qty, orderNumber);
                    log.info("[SAGA-COMPENSATION] Successfully released {} units of SKU: {} for order: {}",
                            qty, sku, orderNumber);
                } catch (Exception e) {
                    log.error("[SAGA-COMPENSATION] Failed to release stock for SKU: {}, order: {}: {}",
                            sku, orderNumber, e.getMessage(), e);
                }
            }
        }
    }

    @KafkaHandler
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("[SAGA-COMPENSATION] Received OrderCancelledEvent for order: {}, reason: {}",
                event.getOrderNumber(), event.getReason());
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object unknown) {
        log.debug("[SAGA-COMPENSATION] Received unknown event on compensation topic: {}", unknown);
    }
}
