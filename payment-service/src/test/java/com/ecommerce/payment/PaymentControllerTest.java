package com.ecommerce.payment;

import com.ecommerce.payment.controller.PaymentController;
import com.ecommerce.payment.model.PaymentRequest;
import com.ecommerce.payment.model.PaymentResponse;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testProcessPaymentEndpoint() throws Exception {
        PaymentRequest request = PaymentRequest.builder()
                .orderNumber("ORD-101")
                .amount(new BigDecimal("199.99"))
                .paymentMethod("CREDIT_CARD")
                .build();

        PaymentResponse response = PaymentResponse.builder()
                .id(1L)
                .orderNumber("ORD-101")
                .amount(new BigDecimal("199.99"))
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentMethod("CREDIT_CARD")
                .transactionId(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentService.createManualPayment(any(PaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-101"))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));
    }

    @Test
    void testGetPaymentByIdEndpoint() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .id(10L)
                .orderNumber("ORD-10")
                .amount(new BigDecimal("49.99"))
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionId("txn-10")
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentService.getPaymentById(10L)).thenReturn(response);

        mockMvc.perform(get("/api/payments/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.orderNumber").value("ORD-10"));
    }

    @Test
    void testGetPaymentByOrderNumberEndpoint() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .id(11L)
                .orderNumber("ORD-20")
                .amount(new BigDecimal("79.99"))
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionId("txn-20")
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentService.getPaymentsByOrderNumber("ORD-20")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/payments/order/ORD-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-20"));
    }
}