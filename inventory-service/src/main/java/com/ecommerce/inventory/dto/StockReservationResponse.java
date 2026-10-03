package com.ecommerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockReservationResponse {
    private String skuCode;
    private boolean reserved;
    private Integer allocatedQuantity;
    private String orderId;
    private String message;
}