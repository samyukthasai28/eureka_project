package com.ecommerce.order.repository;

import com.ecommerce.order.model.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

    Optional<OrderEvent> findByEventId(String eventId);

    List<OrderEvent> findByOrderNumber(String orderNumber);

    List<OrderEvent> findByStatus(String status);

    boolean existsByEventId(String eventId);
}