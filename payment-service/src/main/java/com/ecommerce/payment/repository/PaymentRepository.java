package com.ecommerce.payment.repository;

import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderNumber(String orderNumber);
    Optional<Payment> findTopByOrderNumberOrderByCreatedAtDesc(String orderNumber);
    boolean existsByOrderNumberAndPaymentStatus(String orderNumber, PaymentStatus paymentStatus);
}