package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(request));
    }

    @GetMapping("/{order-number}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("order-number") String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }
}