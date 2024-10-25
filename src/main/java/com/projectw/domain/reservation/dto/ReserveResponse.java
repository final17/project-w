package com.projectw.domain.reservation.dto;

import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;

import java.time.LocalDate;
import java.time.LocalTime;

public sealed interface ReserveResponse permits ReserveResponse.Infos, ReserveResponse.Info, ReserveResponse.ReservationInfo {
    record Infos (
            Long userId,
            Long storeId,
            Long reserveId,
            Long reservationNo,
            Long numberPeople,
            LocalDate reservationDate,
            LocalTime reservationTime,
            ReservationType type,
            ReservationStatus status
    ) implements ReserveResponse {
    }

    record Info (
            Long userId,
            Long storeId,
            Long reserveId,
            Long reservationNo,
            Long numberPeople,
            Long remainReservationCnt,
            LocalDate reservationDate,
            LocalTime reservationTime,
            ReservationType type,
            ReservationStatus status
    ) implements ReserveResponse {

    }

    record ReservationInfo (
            Long storeId,
            Long reservationId
    ) implements ReserveResponse {
    }
}
