package com.ecommerce.payment.service;

import com.ecommerce.common.event.OrderPlacedEvent;
import com.ecommerce.common.event.PaymentFailedEvent;
import com.ecommerce.common.event.PaymentProcessedEvent;
import com.ecommerce.payment.kafka.PaymentEventProducer;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.PaymentRequest;
import com.ecommerce.payment.model.PaymentResponse;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final BigDecimal MAX_ALLOWED_AMOUNT = new BigDecimal("10000.00");

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    public PaymentResponse processOrderPayment(OrderPlacedEvent event) {
        String orderNumber = event.getOrderNumber().toString();
        log.info("[PAYMENT-SERVICE] Processing payment for orderNumber: {}, totalAmount: {}", orderNumber, event.getTotalAmount());

        if (paymentRepository.existsByOrderNumberAndPaymentStatus(orderNumber, PaymentStatus.SUCCESS)) {
            log.warn("[PAYMENT-SERVICE] Order {} already has successful payment. Skipping duplicate processing.", orderNumber);
            Payment existing = paymentRepository.findTopByOrderNumberOrderByCreatedAtDesc(orderNumber).orElseThrow();
            return mapToResponse(existing);
        }

        BigDecimal amount = BigDecimal.valueOf(event.getTotalAmount());
        String paymentMethod = "CREDIT_CARD";

        boolean isSuccess = amount.compareTo(MAX_ALLOWED_AMOUNT) <= 0;

        Payment payment = Payment.builder()
                .orderNumber(orderNumber)
                .amount(amount)
                .paymentStatus(isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .paymentMethod(paymentMethod)
                .transactionId(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("[PAYMENT-SERVICE] Payment record created with id: {}, status: {}", saved.getId(), saved.getPaymentStatus());

        if (isSuccess) {
            PaymentProcessedEvent processedEvent = PaymentProcessedEvent.newBuilder()
                    .setEventId(UUID.randomUUID().toString())
                    .setPaymentId(saved.getId())
                    .setOrderNumber(orderNumber)
                    .setAmount(saved.getAmount().doubleValue())
                    .setPaymentMethod(saved.getPaymentMethod())
                    .setTransactionId(saved.getTransactionId())
                    .setStatus(saved.getPaymentStatus().name())
                    .setTimestamp(Instant.now().toString())
                    .build();
            paymentEventProducer.publishPaymentProcessed(processedEvent);
        } else {
            PaymentFailedEvent failedEvent = PaymentFailedEvent.newBuilder()
                    .setEventId(UUID.randomUUID().toString())
                    .setOrderNumber(orderNumber)
                    .setAmount(saved.getAmount().doubleValue())
                    .setReason("Payment rejected: Amount exceeds transaction limit of $10,000.00")
                    .setTimestamp(Instant.now().toString())
                    .build();
            paymentEventProducer.publishPaymentFailed(failedEvent);
        }

        return mapToResponse(saved);
    }

    @Transactional
    public PaymentResponse createManualPayment(PaymentRequest request) {
        log.info("[PAYMENT-SERVICE] Manual payment requested for order: {}, amount: {}", request.getOrderNumber(), request.getAmount());

        boolean isSuccess = request.getAmount().compareTo(MAX_ALLOWED_AMOUNT) <= 0;

        Payment payment = Payment.builder()
                .orderNumber(request.getOrderNumber())
                .amount(request.getAmount())
                .paymentStatus(isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "DIRECT")
                .transactionId(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        if (isSuccess) {
            PaymentProcessedEvent processedEvent = PaymentProcessedEvent.newBuilder()
                    .setEventId(UUID.randomUUID().toString())
                    .setPaymentId(saved.getId())
                    .setOrderNumber(saved.getOrderNumber())
                    .setAmount(saved.getAmount().doubleValue())
                    .setPaymentMethod(saved.getPaymentMethod())
                    .setTransactionId(saved.getTransactionId())
                    .setStatus(saved.getPaymentStatus().name())
                    .setTimestamp(Instant.now().toString())
                    .build();
            paymentEventProducer.publishPaymentProcessed(processedEvent);
        } else {
            PaymentFailedEvent failedEvent = PaymentFailedEvent.newBuilder()
                    .setEventId(UUID.randomUUID().toString())
                    .setOrderNumber(saved.getOrderNumber())
                    .setAmount(saved.getAmount().doubleValue())
                    .setReason("Payment rejected: Amount exceeds transaction limit of $10,000.00")
                    .setTimestamp(Instant.now().toString())
                    .build();
            paymentEventProducer.publishPaymentFailed(failedEvent);
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderNumber(String orderNumber) {
        return paymentRepository.findByOrderNumber(orderNumber).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderNumber(payment.getOrderNumber())
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}