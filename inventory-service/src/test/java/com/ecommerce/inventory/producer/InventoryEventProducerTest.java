package com.ecommerce.inventory.producer;

import com.ecommerce.common.event.InventoryReservationFailedEvent;
import com.ecommerce.common.event.InventoryReservedEvent;
import com.ecommerce.common.event.ReservedItemRecord;
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

import java.time.Instant;
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
        InventoryReservedEvent event = InventoryReservedEvent.newBuilder()
                .setEventId("EVT-RES-1")
                .setOrderId(10L)
                .setOrderNumber("ORD-998877")
                .setReservedItems(List.of(ReservedItemRecord.newBuilder()
                        .setSkuCode("iphone_15")
                        .setQuantity(1)
                        .setPrice(799.99)
                        .build()))
                .setStatus("RESERVED")
                .setTimestamp(Instant.now().toString())
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
        InventoryReservationFailedEvent event = InventoryReservationFailedEvent.newBuilder()
                .setEventId("EVT-FAIL-2")
                .setOrderId(11L)
                .setOrderNumber("ORD-445566")
                .setReason("Shortage on SKU macbook_pro_16")
                .setShortageSkuCodes(List.of("macbook_pro_16"))
                .setTimestamp(Instant.now().toString())
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