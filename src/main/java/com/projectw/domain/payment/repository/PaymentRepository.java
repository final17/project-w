package com.projectw.domain.payment.repository;

import com.projectw.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p JOIN FETCH p.store s WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderId(String orderId);
}
