package com.projectw.domain.reservation.dto;


import com.projectw.common.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public sealed interface ReserveRequest permits ReserveRequest.Create, ReserveRequest.UpdateStatus {

    record Create(
            Long storeId,
            Long reservationNumber,
            Integer numberOfGuests,
            LocalDate reservationDate,
            LocalTime reservationTime
    ) implements ReserveRequest {}

    record UpdateStatus(ReservationStatus status) implements ReserveRequest {
    }
}
