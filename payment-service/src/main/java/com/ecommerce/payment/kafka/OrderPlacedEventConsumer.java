package com.ecommerce.payment.kafka;

import com.ecommerce.common.event.OrderPlacedEvent;
import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPlacedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedEventConsumer.class);
    private final PaymentService paymentService;

    @KafkaListener(
            topics = "order-placed-topic",
            groupId = "payment-service",
            containerFactory = "orderPlacedKafkaListenerContainerFactory"
    )
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("[KAFKA-CONSUME][payment-service] Received OrderPlacedEvent: orderNumber={}, amount={}",
                event.getOrderNumber(), event.getTotalAmount());
        try {
            paymentService.processOrderPayment(event);
        } catch (Exception e) {
            log.error("[KAFKA-CONSUME][payment-service] Error processing payment for orderNumber {}: {}",
                    event.getOrderNumber(), e.getMessage(), e);
        }
    }
}