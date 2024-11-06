package com.projectw.domain.payment.dto;

import com.projectw.domain.payment.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public sealed interface PaymentRequest permits PaymentRequest.Prepare , PaymentRequest.Susscess , PaymentRequest.Fail , PaymentRequest.Payment {
    record Prepare(
            @NotNull(message = "날짜 데이터는 필수입니다.")
            LocalDate date,
            @NotNull(message = "시간 데이터는 필수입니다.")
            LocalTime time,
            @NotNull(message = "음식점 아이디는 필수입니다.")
            Long storeId,
            @NotNull(message = "금액은 필수입니다.")
            Long amount,
            @NotNull(message = "입장인원값은 필수입니다.")
            Long numberPeople,
            @NotBlank(message = "주문명값은 필수입니다.")
            String orderName
    ) implements PaymentRequest {}

    record Susscess (
            String paymentKey,
            String orderId,
            Long amount
    ) implements PaymentRequest {}

    record Fail (
            String message,
            String code
    ) implements PaymentRequest {}

    record Payment (
            Status status,
            LocalDate startDt,
            LocalDate endDt,
            Integer page,
            Integer size
    ) implements PaymentRequest {
        public Payment {
            if (Objects.isNull(page)) page = 1;
            if (Objects.isNull(size)) size = 10;
        }
    }
}
