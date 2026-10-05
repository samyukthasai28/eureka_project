package com.ecommerce.order.service;

import com.ecommerce.common.event.*;
import com.ecommerce.order.dto.OrderItemDto;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.producer.OrderProducer;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSagaAndResilienceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventStoreService orderEventStoreService;

    @Mock
    private OrderProducer orderProducer;

    @InjectMocks
    private OrderEventHandlerService orderEventHandlerService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderProducer, orderEventStoreService);
    }

    @Test
    @DisplayName("Saga Happy Path: Order CONFIRMED when both inventory and payment succeed")
    void testSagaHappyPathOrderConfirmed() {
        String orderNumber = "ORD-TEST-001";
        Order order = Order.builder()
                .id(1L)
                .orderNumber(orderNumber)
                .customerEmail("customer@example.com")
                .status(OrderStatus.PLACED)
                .inventoryStatus("PENDING")
                .paymentStatus("PENDING")
                .totalAmount(new BigDecimal("299.99"))
                .orderItems(new ArrayList<>())
                .build();

        when(orderEventStoreService.recordInboundEvent(any(), any(), any())).thenReturn(true);
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));

        // Step 1: Inventory reserved event arrives
        InventoryReservedEvent invEvent = InventoryReservedEvent.newBuilder()
                .setEventId("evt-inv-1")
                .setOrderId(1L)
                .setOrderNumber(orderNumber)
                .setStatus("RESERVED")
                .setReservedItems(List.of())
                .setTimestamp(Instant.now().toString())
                .build();

        orderEventHandlerService.handleInventoryReserved(invEvent);
        assertEquals("RESERVED", order.getInventoryStatus());
        assertNotEquals(OrderStatus.CONFIRMED, order.getStatus());

        // Step 2: Payment processed event arrives
        PaymentProcessedEvent payEvent = PaymentProcessedEvent.newBuilder()
                .setEventId("evt-pay-1")
                .setPaymentId(101L)
                .setOrderNumber(orderNumber)
                .setAmount(299.99)
                .setPaymentMethod("CREDIT_CARD")
                .setTransactionId("tx-101")
                .setStatus("SUCCESS")
                .setTimestamp(Instant.now().toString())
                .build();

        orderEventHandlerService.handlePaymentProcessed(payEvent);
        assertEquals("SUCCESS", order.getPaymentStatus());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderRepository, times(2)).save(order);
    }

    @Test
    @DisplayName("Saga Compensation: When payment fails after inventory is reserved, release inventory")
    void testSagaCompensationOnPaymentFailure() {
        String orderNumber = "ORD-TEST-002";
        OrderItem item = OrderItem.builder()
                .skuCode("iphone_15")
                .quantity(2)
                .price(new BigDecimal("999.00"))
                .build();

        List<OrderItem> items = new ArrayList<>();
        items.add(item);

        Order order = Order.builder()
                .id(2L)
                .orderNumber(orderNumber)
                .customerEmail("customer@example.com")
                .status(OrderStatus.PLACED)
                .inventoryStatus("RESERVED")
                .paymentStatus("PENDING")
                .totalAmount(new BigDecimal("1998.00"))
                .orderItems(items)
                .build();

        when(orderEventStoreService.recordInboundEvent(any(), any(), any())).thenReturn(true);
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));

        PaymentFailedEvent payFailedEvent = PaymentFailedEvent.newBuilder()
                .setEventId("evt-pay-fail")
                .setOrderNumber(orderNumber)
                .setAmount(1998.00)
                .setReason("Card declined: Insufficient funds")
                .setTimestamp(Instant.now().toString())
                .build();

        orderEventHandlerService.handlePaymentFailed(payFailedEvent);

        assertEquals("FAILED", order.getPaymentStatus());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        // Verify compensating InventoryReleaseEvent was dispatched
        ArgumentCaptor<InventoryReleaseEvent> releaseCaptor = ArgumentCaptor.forClass(InventoryReleaseEvent.class);
        verify(orderProducer).sendInventoryReleaseEvent(releaseCaptor.capture());
        InventoryReleaseEvent released = releaseCaptor.getValue();
        assertEquals(orderNumber, released.getOrderNumber().toString());
        assertEquals(1, released.getItems().size());
        assertEquals("iphone_15", released.getItems().get(0).getSkuCode().toString());
        assertEquals(2, released.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("Saga Rejection: When inventory shortage occurs, mark order REJECTED")
    void testSagaRejectionOnInventoryShortage() {
        String orderNumber = "ORD-TEST-003";
        Order order = Order.builder()
                .id(3L)
                .orderNumber(orderNumber)
                .customerEmail("customer@example.com")
                .status(OrderStatus.PLACED)
                .inventoryStatus("PENDING")
                .paymentStatus("PENDING")
                .totalAmount(new BigDecimal("49.99"))
                .orderItems(new ArrayList<>())
                .build();

        when(orderEventStoreService.recordInboundEvent(any(), any(), any())).thenReturn(true);
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));

        InventoryReservationFailedEvent invFailedEvent = InventoryReservationFailedEvent.newBuilder()
                .setEventId("evt-inv-shortage")
                .setOrderId(3L)
                .setOrderNumber(orderNumber)
                .setReason("Insufficient stock")
                .setShortageSkuCodes(List.of("out_of_stock_item"))
                .setTimestamp(Instant.now().toString())
                .build();

        orderEventHandlerService.handleInventoryReservationFailed(invFailedEvent);

        assertEquals("FAILED", order.getInventoryStatus());
        assertEquals(OrderStatus.REJECTED, order.getStatus());

        // Verify OrderCancelledEvent was dispatched
        ArgumentCaptor<OrderCancelledEvent> cancelCaptor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(orderProducer).sendOrderCancelledEvent(cancelCaptor.capture());
        assertEquals(orderNumber, cancelCaptor.getValue().getOrderNumber().toString());
    }

    @Test
    @DisplayName("Resilience4j Fallback: Verify placeOrderFallback returns safe degraded response")
    void testOrderPlacementResilienceFallback() {
        OrderRequest request = OrderRequest.builder()
                .customerEmail("user@example.com")
                .items(List.of(OrderItemDto.builder().skuCode("sku-1").quantity(1).price(BigDecimal.TEN).build()))
                .build();

        OrderResponse fallbackResponse = orderService.placeOrderFallback(request, new RuntimeException("Downstream timeout"));

        assertNotNull(fallbackResponse);
        assertTrue(fallbackResponse.getOrderNumber().startsWith("FALLBACK-"));
        assertEquals(OrderStatus.REJECTED, fallbackResponse.getStatus());
        assertEquals("user@example.com", fallbackResponse.getCustomerEmail());
        assertEquals(BigDecimal.ZERO, fallbackResponse.getTotalAmount());
    }
}
