package com.projectw.domain.payment.repository;

import com.projectw.domain.payment.entity.PaymentCancel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCancelRepository extends JpaRepository<PaymentCancel, Long> {
}
