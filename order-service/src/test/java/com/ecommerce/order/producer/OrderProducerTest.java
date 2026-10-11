package com.ecommerce.order.producer;

import com.ecommerce.common.event.OrderItemDto;
import com.ecommerce.common.event.OrderPlacedEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private OrderProducer orderProducer;

    @BeforeEach
    void setUp() {
        orderProducer = new OrderProducer(kafkaTemplate);
        ReflectionTestUtils.setField(orderProducer, "orderPlacedTopic", "order-placed-topic");
    }

    @Test
    @DisplayName("Should dispatch OrderPlacedEvent with correct topic, partition key, and payload")
    void testSendOrderPlacedEventSuccess() {
        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .eventId("EVT-1001")
                .orderId(1L)
                .orderNumber("ORD-12345678")
                .customerEmail("customer@example.com")
                .items(List.of(OrderItemDto.builder().skuCode("iphone_15").quantity(1).price(new BigDecimal("799.99")).build()))
                .totalAmount(new BigDecimal("799.99"))
                .timestamp(LocalDateTime.now())
                .build();

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("order-placed-topic", 0),
                0, 0, System.currentTimeMillis(), 0, 0
        );
        SendResult<String, Object> sendResult = new SendResult<>(null, metadata);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(eq("order-placed-topic"), eq("ORD-12345678"), eq(event)))
                .thenReturn(future);

        CompletableFuture<SendResult<String, Object>> result = orderProducer.sendOrderPlacedEvent(event);

        assertNotNull(result);
        assertTrue(result.isDone());
        verify(kafkaTemplate, times(1)).send("order-placed-topic", "ORD-12345678", event);
    }

    @Test
    @DisplayName("Should handle Kafka broker exception gracefully via callback without uncaught exception")
    void testSendOrderPlacedEventFailureCallback() {
        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .eventId("EVT-FAIL-1")
                .orderNumber("ORD-FAIL-01")
                .customerEmail("test@example.com")
                .build();

        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka broker unreachable"));

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(failedFuture);

        CompletableFuture<SendResult<String, Object>> result = orderProducer.sendOrderPlacedEvent(event);

        assertNotNull(result);
        assertTrue(result.isCompletedExceptionally());
        verify(kafkaTemplate, times(1)).send("order-placed-topic", "ORD-FAIL-01", event);
    }
}