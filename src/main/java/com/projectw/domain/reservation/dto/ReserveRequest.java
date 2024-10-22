package com.projectw.domain.reservation.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public sealed interface ReserveRequest permits ReserveRequest.Wait , ReserveRequest.Reservation , ReserveRequest.Parameter {

    record Reservation(
            boolean menuYN,
            @NotNull(message = "입장인원값은 필수입니다.")
            Long numberPeople,
            @NotBlank(message = "예약날짜는 필수입니다.")
            String reservationDate,
            @NotBlank(message = "예약시간은 필수입니다.")
            String reservationTime
    ) implements ReserveRequest {}

    record Wait(
            boolean menuYN,
            @NotNull(message = "입장인원값은 필수입니다.")
            Long numberPeople
    ) implements ReserveRequest {}

    record Parameter(
            String type,
            String status,
            String startDt,
            String endDt,
            Integer page,
            Integer size
    ) implements ReserveRequest {
        public Parameter {
            if (Objects.isNull(page)) page = 1;
            if (Objects.isNull(size)) size = 10;
        }
    }
}
