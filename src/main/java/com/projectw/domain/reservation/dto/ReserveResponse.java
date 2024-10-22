package com.projectw.domain.reservation.dto;

import com.projectw.common.enums.ReservationStatus;
import com.projectw.domain.reservation.dto.ReserveResponse.Info;
import com.projectw.domain.reservation.entity.Reservation;

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
        public Info(Reservation reservation) {
            this(
                    reservation.getId(),
                    reservation.getUser().getId(),
                    null, //todo store추가 시 추가
                    reservation.getReservationNumber(),
                    reservation.getNumberOfGuests(),
                    reservation.getReservationDate(),
                    reservation.getReservationTime(),
                    reservation.getStatus()
            );
        }
    }
}
