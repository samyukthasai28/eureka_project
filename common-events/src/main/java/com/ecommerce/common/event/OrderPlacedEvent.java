package com.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPlacedEvent implements Serializable {
    private String eventId;
    private Long orderId;
    private String orderNumber;
    private String customerEmail;
    private List<OrderItemDto> items;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
}