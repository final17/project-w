package com.projectw.domain.payment.dto;

public sealed interface PaymentResponse permits PaymentResponse.Prepare , PaymentResponse.Fail {
    record Prepare(
            String orderId,
            String orderName,
            Long amount
    ) implements PaymentResponse {}

    record Fail (
    ) implements PaymentResponse {}
}
