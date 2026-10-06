package com.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryReservedEvent implements Serializable {
    private String eventId;
    private Long orderId;
    private String orderNumber;
    private List<OrderItemDto> reservedItems;
    private LocalDateTime timestamp;
    private String status;
}