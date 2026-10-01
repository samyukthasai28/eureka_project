package com.ecommerce.order.producer;

import com.ecommerce.common.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.order-placed:order-placed-topic}")
    private String orderPlacedTopic;

    public CompletableFuture<SendResult<String, Object>> sendOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("[ORDER-PRODUCER] Dispatching OrderPlacedEvent for order: {} to topic: {}",
                event.getOrderNumber(), orderPlacedTopic);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                orderPlacedTopic,
                event.getOrderNumber().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("[ORDER-PRODUCER] Successfully dispatched OrderPlacedEvent for order {} at offset {}",
                        event.getOrderNumber(), result.getRecordMetadata().offset());
            } else {
                log.error("[ORDER-PRODUCER] Failed to dispatch OrderPlacedEvent for order {}: {}",
                        event.getOrderNumber(), ex.getMessage());
            }
        });

        return future;
    }
}