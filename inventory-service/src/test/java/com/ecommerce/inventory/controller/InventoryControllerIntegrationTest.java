package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.dto.StockReservationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should successfully return inventory details for existing SKU")
    void testGetSingleItemStock() throws Exception {
        mockMvc.perform(get("/api/inventory/iphone_15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skuCode", is("iphone_15")))
                .andExpect(jsonPath("$.inStock", is(true)))
                .andExpect(jsonPath("$.availableQuantity", greaterThan(0)));
    }

    @Test
    @DisplayName("Should batch check multiple SKUs")
    void testBatchCheckStock() throws Exception {
        mockMvc.perform(get("/api/inventory")
                        .param("skuCode", "iphone_15", "macbook_pro_16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].skuCode", containsInAnyOrder("iphone_15", "macbook_pro_16")));
    }

    @Test
    @DisplayName("Should successfully reserve stock for valid quantity")
    void testReserveStockSuccess() throws Exception {
        StockReservationRequest request = StockReservationRequest.builder()
                .skuCode("sony_wh1000xm5")
                .quantity(2)
                .orderId("ORD-INT-TEST-001")
                .build();

        mockMvc.perform(post("/api/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skuCode", is("sony_wh1000xm5")))
                .andExpect(jsonPath("$.reserved", is(true)))
                .andExpect(jsonPath("$.allocatedQuantity", is(2)))
                .andExpect(jsonPath("$.orderId", is("ORD-INT-TEST-001")));
    }

    @Test
    @DisplayName("Should fail reservation when requested quantity exceeds available stock")
    void testReserveStockInsufficientQuantity() throws Exception {
        StockReservationRequest request = StockReservationRequest.builder()
                .skuCode("samsung_s24_ultra")
                .quantity(50)
                .orderId("ORD-FAIL-001")
                .build();

        mockMvc.perform(post("/api/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Insufficient Stock")));
    }

    @Test
    @DisplayName("Should restock inventory when authenticated with internal API key")
    void testRestockWithServiceAuth() throws Exception {
        InventoryRequest request = InventoryRequest.builder()
                .skuCode("pixel_9_pro")
                .quantity(40)
                .unitPrice(new BigDecimal("999.00"))
                .build();

        mockMvc.perform(post("/api/inventory")
                        .header("X-Internal-Token", "secret-internal-microservice-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.skuCode", is("pixel_9_pro")))
                .andExpect(jsonPath("$.availableQuantity", is(40)));
    }

    @Test
    @DisplayName("Should successfully release previously reserved stock")
    void testReleaseStock() throws Exception {
        mockMvc.perform(post("/api/inventory/release")
                        .param("skuCode", "sony_wh1000xm5")
                        .param("quantity", "1")
                        .param("orderId", "ORD-INT-TEST-001"))
                .andExpect(status().isNoContent());
    }
}