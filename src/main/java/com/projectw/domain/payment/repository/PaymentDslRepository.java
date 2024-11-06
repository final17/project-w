package com.projectw.domain.payment.repository;

import com.projectw.domain.payment.dto.PaymentRequest;
import com.projectw.domain.payment.dto.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentDslRepository {
    Page<PaymentResponse.Payment> getPayments(Long userId , PaymentRequest.Payment payment , Pageable pageable);
}
