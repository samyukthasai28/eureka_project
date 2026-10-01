package com.ecommerce.payment.kafka;

import com.ecommerce.common.event.PaymentFailedEvent;
import com.ecommerce.common.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);
    public static final String TOPIC = "payment-events-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        log.info("[KAFKA-PRODUCE] Publishing PaymentProcessedEvent for orderNumber: {}, transactionId: {}",
                event.getOrderNumber(), event.getTransactionId());
        kafkaTemplate.send(TOPIC, event.getOrderNumber().toString(), event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("[KAFKA-PRODUCE] Publishing PaymentFailedEvent for orderNumber: {}, reason: {}",
                event.getOrderNumber(), event.getReason());
        kafkaTemplate.send(TOPIC, event.getOrderNumber().toString(), event);
    }
}