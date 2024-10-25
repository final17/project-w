package com.projectw.domain.payment.dto;

public sealed interface PaymentResponse permits PaymentResponse.Prepare , PaymentResponse.Susscess , PaymentResponse.Fail {
    record Prepare(
            String orderId,
            Long totalAmount
    ) implements PaymentResponse {}

    record Susscess (
    ) implements PaymentResponse {}

    record Fail (
    ) implements PaymentResponse {}
}
