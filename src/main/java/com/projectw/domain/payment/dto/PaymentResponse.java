package com.projectw.domain.payment.dto;

import com.projectw.domain.payment.enums.PaymentMethod;
import com.projectw.domain.payment.enums.Status;

import java.time.OffsetDateTime;

public sealed interface PaymentResponse permits PaymentResponse.Prepare , PaymentResponse.Fail ,PaymentResponse.Payment {
    record Prepare(
            String orderId,
            String orderName,
            Long amount
    ) implements PaymentResponse {}

    record Fail (
    ) implements PaymentResponse {}

    record Payment (
            String paymentKey,          // 결제의 키 값
            String orderId,             // 주문번호
            String orderName,           // 주문명
            Long totalAmount,           // 총금액
            Status status,              // 결제상태(대기 , 완료 , 취소)
            PaymentMethod method,       // 결제수단 : 카드 , 가상계좌 , 간편결제 , 휴대폰 , 계좌이체 , 문화상품권
            OffsetDateTime requestedAt  // 결제날짜
    ) implements PaymentResponse {}
}
