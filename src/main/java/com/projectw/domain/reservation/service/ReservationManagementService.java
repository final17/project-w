package com.projectw.domain.reservation.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.ForbiddenException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.PaymentStatus;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationManagementService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public void refusalReservation(Long userId , Long reservationId) {
        Reservation reservation = reservationRepository.findReservationById(reservationId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

        // 본인 가게 예약건인지 검증
        if (!reservation.getStore().getUser().getId().equals(userId)) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }

        // 거절 가능한지? (예약 상태만 거절 가능)
        if (reservation.getStatus() != ReservationStatus.RESERVATION) {
            throw new ForbiddenException(ResponseCode.REFUSAL_FORBIDDEN);
        }

        reservation.updateStatus(ReservationStatus.CANCEL);

        if (reservation.getPaymentStatus().equals(PaymentStatus.COMP)) {
            // TransactionalEventLister 사용할 것
            // 결제 취소된거
        }
    }

    @Transactional
    public void applyReservation(Long userId , Long reservationId) {
        // 예약 어떤지?
        Reservation reservation = reservationRepository.findReservationById(reservationId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

        // 본인 가게 예약건인지 검증
        if (!reservation.getStore().getUser().getId().equals(userId)) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }

        // 승인 가능한지? (예약 상태만 승인 가능)
        if (reservation.getStatus() != ReservationStatus.RESERVATION || reservation.getPaymentStatus() != PaymentStatus.COMP) {
            throw new ForbiddenException(ResponseCode.APPLY_FORBIDDEN);
        }

        reservation.updateStatus(ReservationStatus.APPLY);
    }

    @Transactional
    public void completeReservation(Long userId , Long reservationId) {
        // 예약 어떤지?
        Reservation reservation = reservationRepository.findReservationById(reservationId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

        // 본인 가게 예약건인지 검증
        if (!reservation.getStore().getUser().getId().equals(userId)) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }

        // 승인 상태만 완료 가능
        if (reservation.getStatus() != ReservationStatus.APPLY) {
            throw new ForbiddenException(ResponseCode.COMPLETE_FORBIDDEN);
        }

        reservation.updateStatus(ReservationStatus.COMPLETE);
    }

    public Page<ReserveResponse.Infos> getOnwerReservations(Long userId , ReserveRequest.Parameter parameter) {
        Pageable pageable = PageRequest.of(parameter.page() - 1, parameter.size());
        return reservationRepository.getOwnerReservations(userId , parameter , pageable);
    }
}
