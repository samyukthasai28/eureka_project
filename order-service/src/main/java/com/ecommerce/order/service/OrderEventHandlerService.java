package com.ecommerce.order.service;

import com.ecommerce.common.event.InventoryReservationFailedEvent;
import com.ecommerce.common.event.InventoryReservedEvent;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderEventHandlerService {

    private static final Logger log = LoggerFactory.getLogger(OrderEventHandlerService.class);
    private final OrderRepository orderRepository;
    private final OrderEventStoreService orderEventStoreService;

    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("[ORDER-EVENT-HANDLER] Processing InventoryReservedEvent for order: {}", event.getOrderNumber());

        if (!orderEventStoreService.recordInboundEvent(event.getEventId(), event.getOrderNumber(), "INVENTORY_RESERVED")) {
            return;
        }

        orderRepository.findByOrderNumber(event.getOrderNumber()).ifPresentOrElse(order -> {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("[ORDER-EVENT-HANDLER] Order {} successfully updated to CONFIRMED", order.getOrderNumber());
        }, () -> log.error("[ORDER-EVENT-HANDLER] Order not found for number: {}", event.getOrderNumber()));
    }

    @Transactional
    public void handleInventoryReservationFailed(InventoryReservationFailedEvent event) {
        log.warn("[ORDER-EVENT-HANDLER] Processing InventoryReservationFailedEvent for order: {}, reason: {}",
                event.getOrderNumber(), event.getReason());

        if (!orderEventStoreService.recordInboundEvent(event.getEventId(), event.getOrderNumber(), "INVENTORY_FAILED")) {
            return;
        }

        orderRepository.findByOrderNumber(event.getOrderNumber()).ifPresentOrElse(order -> {
            order.setStatus(OrderStatus.REJECTED);
            orderRepository.save(order);
            log.info("[ORDER-EVENT-HANDLER] Order {} updated to REJECTED due to stock shortage", order.getOrderNumber());
        }, () -> log.error("[ORDER-EVENT-HANDLER] Order not found for number: {}", event.getOrderNumber()));
    }
}