package com.ecommerce.inventory.producer;

import com.ecommerce.common.event.InventoryReservationFailedEvent;
import com.ecommerce.common.event.InventoryReservedEvent;
import com.ecommerce.common.event.OrderItemDto;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private InventoryEventProducer producer;

    @BeforeEach
    void setUp() {
        producer = new InventoryEventProducer(kafkaTemplate);
        ReflectionTestUtils.setField(producer, "inventoryEventsTopic", "inventory-events-topic");
    }

    @Test
    @DisplayName("Should publish InventoryReservedEvent with orderNumber as message key")
    void testPublishInventoryReservedSuccess() {
        InventoryReservedEvent event = InventoryReservedEvent.builder()
                .eventId("EVT-RES-1")
                .orderId(10L)
                .orderNumber("ORD-998877")
                .reservedItems(List.of(OrderItemDto.builder().skuCode("iphone_15").quantity(1).price(new BigDecimal("799.99")).build()))
                .status("RESERVED")
                .timestamp(LocalDateTime.now())
                .build();

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("inventory-events-topic", 0),
                0, 0, System.currentTimeMillis(), 0, 0
        );
        SendResult<String, Object> sendResult = new SendResult<>(null, metadata);
        when(kafkaTemplate.send(eq("inventory-events-topic"), eq("ORD-998877"), eq(event)))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        producer.publishInventoryReserved(event);

        verify(kafkaTemplate, times(1)).send("inventory-events-topic", "ORD-998877", event);
    }

    @Test
    @DisplayName("Should publish InventoryReservationFailedEvent with orderNumber key on shortage")
    void testPublishInventoryReservationFailed() {
        InventoryReservationFailedEvent event = InventoryReservationFailedEvent.builder()
                .eventId("EVT-FAIL-2")
                .orderId(11L)
                .orderNumber("ORD-445566")
                .reason("Shortage on SKU macbook_pro_16")
                .shortageSkuCodes(List.of("macbook_pro_16"))
                .timestamp(LocalDateTime.now())
                .build();

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("inventory-events-topic", 1),
                0, 0, System.currentTimeMillis(), 0, 0
        );
        SendResult<String, Object> sendResult = new SendResult<>(null, metadata);
        when(kafkaTemplate.send(eq("inventory-events-topic"), eq("ORD-445566"), eq(event)))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        producer.publishInventoryReservationFailed(event);

        verify(kafkaTemplate, times(1)).send("inventory-events-topic", "ORD-445566", event);
    }
}