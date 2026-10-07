package com.ecommerce.order.service;

import com.ecommerce.common.event.OrderItemDto;
import com.ecommerce.common.event.OrderPlacedEvent;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.producer.OrderProducer;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        BigDecimal total = request.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customerEmail(request.getCustomerEmail())
                .status(OrderStatus.PLACED)
                .totalAmount(total)
                .build();

        for (OrderItemDto itemDto : request.getItems()) {
            OrderItem item = OrderItem.builder()
                    .skuCode(itemDto.getSkuCode())
                    .price(itemDto.getPrice())
                    .quantity(itemDto.getQuantity())
                    .build();
            order.addOrderItem(item);
        }

        Order saved = orderRepository.save(order);
        log.info("[ORDER-SERVICE] Created order: {} with total: {}", orderNumber, total);

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(saved.getId())
                .orderNumber(saved.getOrderNumber())
                .customerEmail(saved.getCustomerEmail())
                .items(request.getItems())
                .totalAmount(total)
                .timestamp(LocalDateTime.now())
                .build();

        orderProducer.sendOrderPlacedEvent(event);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with number: " + orderNumber));
        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                .map(i -> OrderItemDto.builder()
                        .skuCode(i.getSkuCode())
                        .price(i.getPrice())
                        .quantity(i.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerEmail(order.getCustomerEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .build();
    }
}