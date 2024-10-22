package com.projectw.domain.reservation.dto;

import com.projectw.common.enums.ReservationStatus;
import com.projectw.domain.reservation.dto.ReserveResponse.Info;

import java.time.LocalDate;
import java.time.LocalTime;

public sealed interface ReserveResponse permits Info {

    record Info (
            Long reserveId,
            Long userId,
            Long storeId,
            Long reservationNumber,
            Integer numberOfGuests,
            LocalDate reservationDate,
            LocalTime reservationTime,
            ReservationStatus status
    ) implements ReserveResponse {
    }
}
