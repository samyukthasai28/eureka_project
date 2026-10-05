package com.ecommerce.payment.controller;

import com.ecommerce.payment.model.PaymentRequest;
import com.ecommerce.payment.model.PaymentResponse;
import com.ecommerce.payment.service.PaymentService;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment Processing & Transaction History APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Process Manual Payment", description = "Executes payment transaction for an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment created and processed"),
            @ApiResponse(responseCode = "400", description = "Invalid payment input")
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createManualPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get Payment by ID", description = "Retrieves payment record by primary key")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @Operation(summary = "Get Payments by Order Number", description = "Retrieves payment history for a given order number")
    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrderNumber(orderNumber));
    }
}