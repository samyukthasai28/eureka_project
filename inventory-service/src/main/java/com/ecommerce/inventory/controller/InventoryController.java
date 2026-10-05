package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock Management & Reservation APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Get Inventory by SKU", description = "Fetches stock details for a specific SKU code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventory record found"),
            @ApiResponse(responseCode = "404", description = "Inventory record not found")
    })
    @GetMapping("/{sku-code}")
    public ResponseEntity<InventoryResponse> getBySku(@PathVariable("sku-code") String skuCode) {
        return ResponseEntity.ok(inventoryService.getInventoryBySku(skuCode));
    }

    @Operation(summary = "Check SKU Availability", description = "Queries available stock for multiple SKU codes")
    @GetMapping
    public ResponseEntity<List<InventoryResponse>> isInStock(@RequestParam(name = "skuCode", required = false) List<String> skuCodes) {
        if (skuCodes == null || skuCodes.isEmpty()) {
            return ResponseEntity.ok(inventoryService.getAllInventory());
        }
        return ResponseEntity.ok(inventoryService.isInStock(skuCodes));
    }

    @Operation(summary = "Get All Inventory", description = "Retrieves all stock items in catalog")
    @GetMapping("/all")
    public ResponseEntity<List<InventoryResponse>> getAll() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @Operation(summary = "Create or Update Inventory", description = "Adds new inventory or updates quantity/price of existing SKU")
    @PostMapping
    public ResponseEntity<InventoryResponse> createOrUpdate(@RequestBody InventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createOrUpdateInventory(request));
    }

    @Operation(summary = "Reserve Stock", description = "Reserves stock units for a specific order")
    @PostMapping("/reserve")
    public ResponseEntity<StockReservationResponse> reserveStock(@RequestBody StockReservationRequest request) {
        return ResponseEntity.ok(inventoryService.reserveStock(request));
    }

    @Operation(summary = "Release Stock", description = "Releases previously reserved stock back to available inventory")
    @PostMapping("/release")
    public ResponseEntity<Void> releaseStock(@RequestParam String skuCode,
                                             @RequestParam int quantity,
                                             @RequestParam(required = false) String orderId) {
        inventoryService.releaseStock(skuCode, quantity, orderId);
        return ResponseEntity.noContent().build();
    }
}