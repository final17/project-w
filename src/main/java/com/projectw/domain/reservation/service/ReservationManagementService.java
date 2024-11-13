package com.projectw.domain.reservation.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.ForbiddenException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.domain.payment.event.PaymentCancelEvent;
import com.projectw.domain.reservation.component.ReservationCheckService;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ReservationCheckService reservationCheckService;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void refusalReservation(Long userId , Long reservationId , ReserveRequest.Cancel cancel) {
        Reservation reservation = reservationRepository.findReservationById(reservationId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

        // 본인 가게 예약건인지 검증
        reservationCheckService.isOwnerReservation(userId , reservation);

        // 거절 가능한지? (예약 상태만 거절 가능)
        reservationCheckService.canChangeReservationStatus(reservation , ReservationStatus.RESERVATION , ResponseCode.REFUSAL_FORBIDDEN);


        reservation.updateStatus(ReservationStatus.CANCEL);

        // PaymentEventListener 결제취소
        if (reservation.isPaymentYN()){
            PaymentCancelEvent paymentCancelEvent = new PaymentCancelEvent(reservation.getOrderId() , cancel.cancelReason());
            eventPublisher.publishEvent(paymentCancelEvent);
        }
    }

    @Transactional
    public void applyReservation(Long userId , Long reservationId) {
        // 예약 어떤지?
        Reservation reservation = reservationRepository.findReservationById(reservationId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

        // 본인 가게 예약건인지 검증
        reservationCheckService.isOwnerReservation(userId , reservation);

        // 승인 가능한지? (예약 상태만 승인 가능)
        reservationCheckService.canChangeReservationStatus(reservation , ReservationStatus.RESERVATION , ResponseCode.APPLY_FORBIDDEN);

        reservation.updateStatus(ReservationStatus.APPLY);
    }

    @Transactional
    public void completeReservation(Long userId , Long reservationId) {
        // 예약 어떤지?
        Reservation reservation = reservationRepository.findReservationById(reservationId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

        // 본인 가게 예약건인지 검증
        reservationCheckService.isOwnerReservation(userId , reservation);

        // 승인 상태만 완료 가능
        reservationCheckService.canChangeReservationStatus(reservation , ReservationStatus.APPLY , ResponseCode.COMPLETE_FORBIDDEN);

        reservation.updateStatus(ReservationStatus.COMPLETE);
    }

    public Page<ReserveResponse.Infos> getOnwerReservations(Long userId , ReserveRequest.OwnerParameter ownerParameter) {
        Pageable pageable = PageRequest.of(ownerParameter.page() - 1, ownerParameter.size());
        return reservationRepository.getOwnerReservations(userId , ownerParameter , pageable);
    }
}
