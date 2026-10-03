package com.ecommerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryResponse {
    private String skuCode;
    private boolean isInStock;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private BigDecimal unitPrice;
}