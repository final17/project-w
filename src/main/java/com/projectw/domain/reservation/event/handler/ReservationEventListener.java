package com.projectw.domain.reservation.event.handler;

import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.event.ReservationInsertEvent;
import com.projectw.domain.reservation.event.ReservationPaymentCompEvent;
import com.projectw.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final ReservationService reservationService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleReservationInsertEvent(ReservationInsertEvent event) {
        log.info("handleReservationInsertEvent");
        ReserveRequest.InsertReservation insertReservation = new ReserveRequest.InsertReservation(event.getOrderId() , event.getDate() , event.getTime(), event.getNumberPeople() , event.getPaymentAmt() , event.getUser() , event.getStore());
        reservationService.prepareReservation(insertReservation);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleReservationPaymentCompEvent(ReservationPaymentCompEvent event) {
        log.info("handleReservationPaymentCompEvent");
        reservationService.successReservation(event.getOrderId());
    }
}
