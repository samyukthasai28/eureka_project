package com.ecommerce.inventory.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_inventory")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_code", nullable = false, unique = true)
    private String skuCode;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.reservedQuantity == null) {
            this.reservedQuantity = 0;
        }
        if (this.quantity == null) {
            this.quantity = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public int getAvailableQuantity() {
        return Math.max(0, (this.quantity != null ? this.quantity : 0) - (this.reservedQuantity != null ? this.reservedQuantity : 0));
    }

    public boolean isInStock() {
        return getAvailableQuantity() > 0;
    }

    public boolean canReserve(int count) {
        return getAvailableQuantity() >= count;
    }

    public void reserve(int count) {
        if (!canReserve(count)) {
            throw new IllegalStateException("Insufficient available stock for SKU " + this.skuCode);
        }
        this.reservedQuantity = (this.reservedQuantity != null ? this.reservedQuantity : 0) + count;
    }

    public void release(int count) {
        this.reservedQuantity = Math.max(0, (this.reservedQuantity != null ? this.reservedQuantity : 0) - count);
    }

    public void deduct(int count) {
        if (this.quantity < count) {
            throw new IllegalStateException("Cannot deduct more than current quantity for SKU " + this.skuCode);
        }
        this.quantity -= count;
        this.reservedQuantity = Math.max(0, (this.reservedQuantity != null ? this.reservedQuantity : 0) - count);
    }
}