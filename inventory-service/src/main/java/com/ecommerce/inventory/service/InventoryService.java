package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.exception.InsufficientStockException;
import com.ecommerce.inventory.exception.InventoryNotFoundException;
import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCodes) {
        log.info("[INVENTORY-SERVICE] Checking stock for SKUs: {}", skuCodes);
        return inventoryRepository.findBySkuCodeIn(skuCodes).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryBySku(String skuCode) {
        Inventory item = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for SKU: " + skuCode));
        return mapToResponse(item);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryResponse createOrUpdateInventory(InventoryRequest request) {
        Inventory inventory = inventoryRepository.findBySkuCode(request.getSkuCode())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + request.getQuantity());
                    if (request.getUnitPrice() != null) {
                        existing.setUnitPrice(request.getUnitPrice());
                    }
                    return existing;
                })
                .orElseGet(() -> Inventory.builder()
                        .skuCode(request.getSkuCode())
                        .quantity(request.getQuantity())
                        .reservedQuantity(0)
                        .unitPrice(request.getUnitPrice())
                        .build());

        Inventory saved = inventoryRepository.save(inventory);
        log.info("[INVENTORY-SERVICE] Updated stock for SKU: {}, available: {}", saved.getSkuCode(), saved.getAvailableQuantity());
        return mapToResponse(saved);
    }

    @Transactional
    public StockReservationResponse reserveStock(StockReservationRequest request) {
        Inventory inventory = inventoryRepository.findBySkuCode(request.getSkuCode())
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for SKU: " + request.getSkuCode()));

        if (!inventory.canReserve(request.getQuantity())) {
            log.warn("[INVENTORY-SERVICE] Reservation failed for SKU {} (requested {}, available {})",
                    request.getSkuCode(), request.getQuantity(), inventory.getAvailableQuantity());
            throw new InsufficientStockException("Insufficient stock for SKU " + request.getSkuCode() +
                    ". Available: " + inventory.getAvailableQuantity());
        }

        inventory.reserve(request.getQuantity());
        inventoryRepository.save(inventory);
        log.info("[INVENTORY-SERVICE] Successfully reserved {} items of SKU {} for order {}",
                request.getQuantity(), request.getSkuCode(), request.getOrderId());

        return StockReservationResponse.builder()
                .skuCode(request.getSkuCode())
                .reserved(true)
                .allocatedQuantity(request.getQuantity())
                .orderId(request.getOrderId())
                .message("Stock successfully reserved")
                .build();
    }

    @Transactional
    public void releaseStock(String skuCode, int quantity, String orderId) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for SKU: " + skuCode));

        inventory.release(quantity);
        inventoryRepository.save(inventory);
        log.info("[INVENTORY-SERVICE] Released {} items of SKU {} for order {}", quantity, skuCode, orderId);
    }

    private InventoryResponse mapToResponse(Inventory item) {
        return InventoryResponse.builder()
                .skuCode(item.getSkuCode())
                .isInStock(item.isInStock())
                .availableQuantity(item.getAvailableQuantity())
                .reservedQuantity(item.getReservedQuantity())
                .unitPrice(item.getUnitPrice())
                .build();
    }
}