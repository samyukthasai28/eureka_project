package com.ecommerce.order.service;

import com.ecommerce.common.event.InventoryReleaseEvent;
import com.ecommerce.common.event.InventoryReservationFailedEvent;
import com.ecommerce.common.event.InventoryReservedEvent;
import com.ecommerce.common.event.OrderCancelledEvent;
import com.ecommerce.common.event.PaymentFailedEvent;
import com.ecommerce.common.event.PaymentProcessedEvent;
import com.ecommerce.common.event.ReleaseItemRecord;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.producer.OrderProducer;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderEventHandlerService {

    private static final Logger log = LoggerFactory.getLogger(OrderEventHandlerService.class);
    private final OrderRepository orderRepository;
    private final OrderEventStoreService orderEventStoreService;
    private final OrderProducer orderProducer;

    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        String orderNumber = event.getOrderNumber().toString();
        String eventId = event.getEventId().toString();
        log.info("[SAGA] Processing InventoryReservedEvent for order: {}", orderNumber);

        if (!orderEventStoreService.recordInboundEvent(eventId, orderNumber, "INVENTORY_RESERVED")) {
            return;
        }

        orderRepository.findByOrderNumber(orderNumber).ifPresentOrElse(order -> {
            order.setInventoryStatus("RESERVED");

            if ("SUCCESS".equalsIgnoreCase(order.getPaymentStatus())) {
                order.setStatus(OrderStatus.CONFIRMED);
                log.info("[SAGA] Order {} CONFIRMED: Inventory reserved and payment successful", orderNumber);
            } else if ("FAILED".equalsIgnoreCase(order.getPaymentStatus())) {
                // Payment previously failed, compensate inventory immediately
                order.setStatus(OrderStatus.CANCELLED);
                log.warn("[SAGA] Order {} CANCELLED: Payment had already failed. Triggering inventory release compensation.", orderNumber);
                triggerInventoryReleaseCompensation(order, "Payment previously failed");
            } else {
                log.info("[SAGA] Order {} inventory reserved, awaiting payment result", orderNumber);
            }
            orderRepository.save(order);
        }, () -> log.error("[SAGA] Order not found for number: {}", orderNumber));
    }

    @Transactional
    public void handleInventoryReservationFailed(InventoryReservationFailedEvent event) {
        String orderNumber = event.getOrderNumber().toString();
        String eventId = event.getEventId().toString();
        String reason = event.getReason().toString();
        log.warn("[SAGA] Processing InventoryReservationFailedEvent for order: {}, reason: {}", orderNumber, reason);

        if (!orderEventStoreService.recordInboundEvent(eventId, orderNumber, "INVENTORY_FAILED")) {
            return;
        }

        orderRepository.findByOrderNumber(orderNumber).ifPresentOrElse(order -> {
            order.setInventoryStatus("FAILED");
            order.setStatus(OrderStatus.REJECTED);
            orderRepository.save(order);
            log.info("[SAGA] Order {} updated to REJECTED due to stock shortage", orderNumber);

            // Notify saga participants of cancellation
            OrderCancelledEvent cancelledEvent = OrderCancelledEvent.newBuilder()
                    .setEventId(UUID.randomUUID().toString())
                    .setOrderId(order.getId() != null ? order.getId() : 0L)
                    .setOrderNumber(orderNumber)
                    .setReason("Inventory shortage: " + reason)
                    .setTimestamp(Instant.now().toString())
                    .build();
            orderProducer.sendOrderCancelledEvent(cancelledEvent);
        }, () -> log.error("[SAGA] Order not found for number: {}", orderNumber));
    }

    @Transactional
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        String orderNumber = event.getOrderNumber().toString();
        String eventId = event.getEventId().toString();
        log.info("[SAGA] Processing PaymentProcessedEvent for order: {}", orderNumber);

        if (!orderEventStoreService.recordInboundEvent(eventId, orderNumber, "PAYMENT_PROCESSED")) {
            return;
        }

        orderRepository.findByOrderNumber(orderNumber).ifPresentOrElse(order -> {
            order.setPaymentStatus("SUCCESS");

            if ("RESERVED".equalsIgnoreCase(order.getInventoryStatus())) {
                order.setStatus(OrderStatus.CONFIRMED);
                log.info("[SAGA] Order {} CONFIRMED: Payment successful and inventory reserved", orderNumber);
            } else if ("FAILED".equalsIgnoreCase(order.getInventoryStatus())) {
                order.setStatus(OrderStatus.CANCELLED);
                log.warn("[SAGA] Order {} CANCELLED: Payment succeeded but inventory previously failed. Triggering refund/cancellation.", orderNumber);
                OrderCancelledEvent cancelledEvent = OrderCancelledEvent.newBuilder()
                        .setEventId(UUID.randomUUID().toString())
                        .setOrderId(order.getId() != null ? order.getId() : 0L)
                        .setOrderNumber(orderNumber)
                        .setReason("Inventory reservation failed")
                        .setTimestamp(Instant.now().toString())
                        .build();
                orderProducer.sendOrderCancelledEvent(cancelledEvent);
            } else {
                log.info("[SAGA] Order {} payment processed, awaiting inventory reservation result", orderNumber);
            }
            orderRepository.save(order);
        }, () -> log.error("[SAGA] Order not found for number: {}", orderNumber));
    }

    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        String orderNumber = event.getOrderNumber().toString();
        String eventId = event.getEventId().toString();
        String reason = event.getReason().toString();
        log.warn("[SAGA] Processing PaymentFailedEvent for order: {}, reason: {}", orderNumber, reason);

        if (!orderEventStoreService.recordInboundEvent(eventId, orderNumber, "PAYMENT_FAILED")) {
            return;
        }

        orderRepository.findByOrderNumber(orderNumber).ifPresentOrElse(order -> {
            order.setPaymentStatus("FAILED");
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("[SAGA] Order {} updated to CANCELLED due to payment failure", orderNumber);

            // If inventory was reserved, execute compensation to release stock
            if ("RESERVED".equalsIgnoreCase(order.getInventoryStatus())) {
                log.info("[SAGA] Compensating inventory reservation for cancelled order: {}", orderNumber);
                triggerInventoryReleaseCompensation(order, "Payment failed: " + reason);
            } else {
                OrderCancelledEvent cancelledEvent = OrderCancelledEvent.newBuilder()
                        .setEventId(UUID.randomUUID().toString())
                        .setOrderId(order.getId() != null ? order.getId() : 0L)
                        .setOrderNumber(orderNumber)
                        .setReason("Payment failed: " + reason)
                        .setTimestamp(Instant.now().toString())
                        .build();
                orderProducer.sendOrderCancelledEvent(cancelledEvent);
            }
        }, () -> log.error("[SAGA] Order not found for number: {}", orderNumber));
    }

    private void triggerInventoryReleaseCompensation(Order order, String reason) {
        List<ReleaseItemRecord> releaseItems = order.getOrderItems().stream()
                .map(item -> ReleaseItemRecord.newBuilder()
                        .setSkuCode(item.getSkuCode())
                        .setQuantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        InventoryReleaseEvent releaseEvent = InventoryReleaseEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setOrderId(order.getId() != null ? order.getId() : 0L)
                .setOrderNumber(order.getOrderNumber())
                .setReason(reason)
                .setItems(releaseItems)
                .setTimestamp(Instant.now().toString())
                .build();

        orderProducer.sendInventoryReleaseEvent(releaseEvent);
        log.info("[SAGA] Dispatched InventoryReleaseEvent for order: {} with {} items",
                order.getOrderNumber(), releaseItems.size());
    }
}