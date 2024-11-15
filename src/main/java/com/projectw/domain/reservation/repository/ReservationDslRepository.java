package com.projectw.domain.reservation.repository;

import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationDslRepository {
    public Page<ReserveResponse.Infos> getOwnerReservations(Long userId, Long storeId, ReserveRequest.OwnerParameter parameter, Pageable pageable);

    Page<ReserveResponse.Infos> getUserReservations(Long userId , ReserveRequest.Parameter parameter , Pageable pageable);
}
