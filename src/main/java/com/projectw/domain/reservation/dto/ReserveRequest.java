package com.projectw.domain.reservation.dto;


import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public sealed interface ReserveRequest permits ReserveRequest.Wait , ReserveRequest.Reservation , ReserveRequest.Parameter , ReserveRequest.PaymentInfo {

    record Reservation(
            boolean menuYN,
            @NotNull(message = "입장인원값은 필수입니다.")
            Long numberPeople,
            @NotBlank(message = "예약날짜는 필수입니다.")
            LocalDate reservationDate,
            @NotBlank(message = "예약시간은 필수입니다.")
            LocalTime reservationTime,
            @NotNull(message = "예약금은 필수입니다.")
            Long paymentAmt
    ) implements ReserveRequest {}

    record Wait(
            boolean menuYN,
            @NotNull(message = "입장인원값은 필수입니다.")
            Long numberPeople
    ) implements ReserveRequest {}

    record Parameter(
            ReservationType type,
            ReservationStatus status,
            LocalDate startDt,
            LocalDate endDt,
            Integer page,
            Integer size
    ) implements ReserveRequest {
        public Parameter {
            if (Objects.isNull(page)) page = 1;
            if (Objects.isNull(size)) size = 10;
        }
    }

    record PaymentInfo(
            Long revervationId,
            String paymentKey,
            String orderId,
            String amount
    ) implements ReserveRequest {

    }
}
