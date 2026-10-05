package com.ecommerce.order.consumer;

import com.ecommerce.common.event.PaymentFailedEvent;
import com.ecommerce.common.event.PaymentProcessedEvent;
import com.ecommerce.order.service.OrderEventHandlerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = "${app.kafka.topics.payment-events:payment-events-topic}", groupId = "order-service")
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);
    private final OrderEventHandlerService orderEventHandlerService;

    @KafkaHandler
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("[ORDER-CONSUMER] Received PaymentProcessedEvent for order: {}", event.getOrderNumber());
        orderEventHandlerService.handlePaymentProcessed(event);
    }

    @KafkaHandler
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("[ORDER-CONSUMER] Received PaymentFailedEvent for order: {}", event.getOrderNumber());
        orderEventHandlerService.handlePaymentFailed(event);
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object unknown) {
        log.debug("[ORDER-CONSUMER] Received unhandled payment event payload: {}", unknown);
    }
}
