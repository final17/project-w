package com.projectw.domain.payment.repository;

import com.projectw.domain.payment.entity.Payment;
import com.projectw.domain.payment.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> , PaymentDslRepository {
    @Query("SELECT p FROM Payment p JOIN FETCH p.store s WHERE p.orderId = :orderId AND p.status = :status")
    Optional<Payment> findByOrderIdAndStatus(String orderId , Status status);
}
