package com.projectw.domain.reservation.service;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.InvalidRequestException;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.security.AuthUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * 예약 생성
     * @param authUser
     * @param createRequest
     * @return
     */
    public SuccessResponse<ReserveResponse.Info> reserve(AuthUser authUser, long storeId, ReserveRequest.Create createRequest) {
        User user = User.fromAuthUser(authUser);

        //todo temp
        /*
        Store store = storeRepository.findById(storeId).orElseThrow(()-> new InvalidRequestException(ResponseCode.NOT_FOUND_STORE))
        */

        Reservation reservation = Reservation.builder()
                //.store(store)
                .user(user)
                .reservationNumber(createRequest.reservationNumber())
                .reservationDate(createRequest.reservationDate())
                .reservationTime(createRequest.reservationTime())
                .numberOfGuests(createRequest.numberOfGuests())
                .build();

        reservation = reservationRepository.save(reservation);
        return SuccessResponse.of(new ReserveResponse.Info(reservation));
    }

    /**
     * 예약 상태 변경
     * @param authUser 가게 사장만 가능
     * @param storeId
     * @param reservationId
     * @param request
     * @return
     */
    public SuccessResponse<ReserveResponse.Info> reservationStatusChange(AuthUser authUser, long storeId, long reservationId, ReserveRequest.UpdateStatus request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new InvalidRequestException(ResponseCode.NOT_FOUND_RESERVATION));

        return SuccessResponse.of(new ReserveResponse.Info(reservation));
    }
}
