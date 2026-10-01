package com.ecommerce.payment;

import com.ecommerce.common.event.OrderPlacedEvent;
import com.ecommerce.common.event.PaymentFailedEvent;
import com.ecommerce.common.event.PaymentProcessedEvent;
import com.ecommerce.payment.kafka.PaymentEventProducer;
import com.ecommerce.payment.model.PaymentRequest;
import com.ecommerce.payment.model.PaymentResponse;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockBean
    private PaymentEventProducer paymentEventProducer;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    void testProcessOrderPayment_Success() {
        OrderPlacedEvent event = OrderPlacedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setOrderId(1L)
                .setOrderNumber("ORD-1001")
                .setCustomerEmail("customer@example.com")
                .setTotalAmount(500.0)
                .setItems(Collections.emptyList())
                .setTimestamp(Instant.now().toString())
                .build();

        PaymentResponse response = paymentService.processOrderPayment(event);

        assertNotNull(response);
        assertEquals("ORD-1001", response.getOrderNumber());
        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertNotNull(response.getTransactionId());
        verify(paymentEventProducer, times(1)).publishPaymentProcessed(any(PaymentProcessedEvent.class));
        verify(paymentEventProducer, never()).publishPaymentFailed(any(PaymentFailedEvent.class));
    }

    @Test
    void testProcessOrderPayment_FailureWhenExceeding10000() {
        OrderPlacedEvent event = OrderPlacedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setOrderId(2L)
                .setOrderNumber("ORD-9999")
                .setCustomerEmail("whale@example.com")
                .setTotalAmount(15000.0)
                .setItems(Collections.emptyList())
                .setTimestamp(Instant.now().toString())
                .build();

        PaymentResponse response = paymentService.processOrderPayment(event);

        assertNotNull(response);
        assertEquals("ORD-9999", response.getOrderNumber());
        assertEquals(PaymentStatus.FAILED, response.getPaymentStatus());
        verify(paymentEventProducer, times(1)).publishPaymentFailed(any(PaymentFailedEvent.class));
        verify(paymentEventProducer, never()).publishPaymentProcessed(any(PaymentProcessedEvent.class));
    }

    @Test
    void testProcessOrderPayment_Idempotency() {
        OrderPlacedEvent event = OrderPlacedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setOrderId(3L)
                .setOrderNumber("ORD-IDEMPOTENT")
                .setCustomerEmail("idem@example.com")
                .setTotalAmount(250.0)
                .setItems(Collections.emptyList())
                .setTimestamp(Instant.now().toString())
                .build();

        PaymentResponse first = paymentService.processOrderPayment(event);
        assertEquals(PaymentStatus.SUCCESS, first.getPaymentStatus());

        // Process again with same orderNumber
        PaymentResponse second = paymentService.processOrderPayment(event);
        assertEquals(first.getId(), second.getId());
        assertEquals(first.getTransactionId(), second.getTransactionId());

        // Verify event producer was only called once
        verify(paymentEventProducer, times(1)).publishPaymentProcessed(any(PaymentProcessedEvent.class));
    }

    @Test
    void testManualPayment_CreateAndQuery() {
        PaymentRequest request = PaymentRequest.builder()
                .orderNumber("ORD-MANUAL-1")
                .amount(new BigDecimal("120.50"))
                .paymentMethod("PAYPAL")
                .build();

        PaymentResponse created = paymentService.createManualPayment(request);
        assertNotNull(created.getId());
        assertEquals(PaymentStatus.SUCCESS, created.getPaymentStatus());

        PaymentResponse fetched = paymentService.getPaymentById(created.getId());
        assertEquals(created.getId(), fetched.getId());
        assertEquals("ORD-MANUAL-1", fetched.getOrderNumber());
    }
}