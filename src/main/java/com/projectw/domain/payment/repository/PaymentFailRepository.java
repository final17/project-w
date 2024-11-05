package com.projectw.domain.payment.repository;

import com.projectw.domain.payment.entity.PaymentFail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentFailRepository extends JpaRepository<PaymentFail, Long> {
}
