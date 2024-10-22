package com.projectw.domain.reservation.repository;

import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationDslRepository {
    Page<ReserveResponse.Infos> getOwnerReservations(Long userId , ReserveRequest.Parameter parameter , Pageable pageable);

    Page<ReserveResponse.Infos> getUserReservations(Long userId , ReserveRequest.Parameter parameter , Pageable pageable);

    ReserveResponse.Info getReservation(Long userId , Long reservationId);
}
