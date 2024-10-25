package com.projectw.domain.payment.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public sealed interface PaymentRequest permits PaymentRequest.Prepare , PaymentRequest.Susscess , PaymentRequest.Fail {
    record Prepare(
            @NotNull(message = "날짜 데이터는 필수입니다.")
            LocalDate date,
            @NotNull(message = "시간 데이터는 필수입니다.")
            LocalTime time,
            @NotNull(message = "음식점 아이디는 필수입니다.")
            Long storeId,
            @NotNull(message = "예약금은 필수입니다.")
            Long amount,
            @NotNull(message = "입장인원값은 필수입니다.")
            Long numberPeople
    ) implements PaymentRequest {}

    record Susscess (
            String paymentKey,
            String orderId,
            Long amount
    ) implements PaymentRequest {}

    record Fail (
            String message,
            int code
    ) implements PaymentRequest {}
}
