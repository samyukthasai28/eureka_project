package com.ecommerce.order.dto;

import com.ecommerce.common.event.OrderItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    private String customerEmail;
    private List<OrderItemDto> items;
}