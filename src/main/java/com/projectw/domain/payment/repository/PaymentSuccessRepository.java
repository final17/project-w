package com.projectw.domain.payment.repository;

import com.projectw.domain.payment.entity.PaymentSuccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentSuccessRepository extends JpaRepository<PaymentSuccess, Long> {
    Optional<PaymentSuccess> findByOrderId(String orderId);
}
