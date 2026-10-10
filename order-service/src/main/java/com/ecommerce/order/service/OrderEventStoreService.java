package com.ecommerce.order.service;

import com.ecommerce.order.model.OrderEvent;
import com.ecommerce.order.repository.OrderEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderEventStoreService {

    private static final Logger log = LoggerFactory.getLogger(OrderEventStoreService.class);
    private final OrderEventRepository orderEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderEvent saveOutboundEvent(String eventId, Long orderId, String orderNumber, String eventType, Object payload) {
        String jsonPayload = null;
        try {
            jsonPayload = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("[EVENT-STORE] Failed to serialize payload for event {}", eventId, e);
        }

        OrderEvent event = OrderEvent.builder()
                .eventId(eventId)
                .orderId(orderId)
                .orderNumber(orderNumber)
                .eventType(eventType)
                .payload(jsonPayload)
                .status("PUBLISHED")
                .createdAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .build();

        OrderEvent saved = orderEventRepository.save(event);
        log.info("[EVENT-STORE] Persisted outbound event {} ({}) for order {}", eventId, eventType, orderNumber);
        return saved;
    }

    @Transactional
    public boolean recordInboundEvent(String eventId, String orderNumber, String eventType) {
        if (orderEventRepository.existsByEventId(eventId)) {
            log.warn("[EVENT-STORE] Inbound event {} already processed. Skipping duplicate.", eventId);
            return false;
        }

        OrderEvent event = OrderEvent.builder()
                .eventId(eventId)
                .orderNumber(orderNumber)
                .eventType(eventType)
                .status("PROCESSED")
                .createdAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .build();

        orderEventRepository.save(event);
        log.info("[EVENT-STORE] Recorded inbound event {} ({}) for order {}", eventId, eventType, orderNumber);
        return true;
    }

    @Transactional(readOnly = true)
    public List<OrderEvent> getEventsByOrderNumber(String orderNumber) {
        return orderEventRepository.findByOrderNumber(orderNumber);
    }

    @Transactional(readOnly = true)
    public Optional<OrderEvent> getByEventId(String eventId) {
        return orderEventRepository.findByEventId(eventId);
    }
}