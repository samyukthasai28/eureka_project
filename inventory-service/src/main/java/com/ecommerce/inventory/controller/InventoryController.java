package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{sku-code}")
    public ResponseEntity<InventoryResponse> getBySku(@PathVariable("sku-code") String skuCode) {
        return ResponseEntity.ok(inventoryService.getInventoryBySku(skuCode));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> isInStock(@RequestParam(name = "skuCode", required = false) List<String> skuCodes) {
        if (skuCodes == null || skuCodes.isEmpty()) {
            return ResponseEntity.ok(inventoryService.getAllInventory());
        }
        return ResponseEntity.ok(inventoryService.isInStock(skuCodes));
    }

    @GetMapping("/all")
    public ResponseEntity<List<InventoryResponse>> getAll() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> createOrUpdate(@RequestBody InventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createOrUpdateInventory(request));
    }

    @PostMapping("/reserve")
    public ResponseEntity<StockReservationResponse> reserveStock(@RequestBody StockReservationRequest request) {
        return ResponseEntity.ok(inventoryService.reserveStock(request));
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseStock(@RequestParam String skuCode,
                                             @RequestParam int quantity,
                                             @RequestParam(required = false) String orderId) {
        inventoryService.releaseStock(skuCode, quantity, orderId);
        return ResponseEntity.noContent().build();
    }
}