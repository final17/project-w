package com.projectw.domain.reservation.service;

import com.projectw.common.annotations.RedisListener;
import com.projectw.common.annotations.RedisLock;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.ForbiddenException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.common.exceptions.UnauthorizedException;
import com.projectw.common.utils.Scheduler;
import com.projectw.domain.payment.event.PaymentCancelEvent;
import com.projectw.domain.payment.event.PaymentTimeoutCancelEvent;
import com.projectw.domain.reservation.component.ReservationCheckService;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.domain.waiting.dto.WaitingPoll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    private final ReservationCheckService reservationCheckService;

    private final ApplicationEventPublisher eventPublisher;
    private final Scheduler scheduler;

    @Transactional
    public void prepareReservation(ReserveRequest.InsertReservation insertReservation) {
        // 예약번호 채번하기
        LocalDate now = LocalDate.now();
        Long reservationNo = reservationRepository.findMaxReservationDate(ReservationType.RESERVATION , now);

        // 예약 Entity 만들기
        Reservation reservation = Reservation.builder()
                .orderId(insertReservation.orderId())
                .status(ReservationStatus.RESERVATION)      // 예약 단계!!(승인 안된 상태!)
                .type(ReservationType.RESERVATION)          // 웨이팅 , 예약 중 예약이라는 의미
                .reservationDate(insertReservation.reservationDate())
                .reservationTime(insertReservation.reservationTime())
                .numberPeople(insertReservation.numberPeople())
                .reservationNo(reservationNo)
                .menuYN(insertReservation.menuYN())
                .paymentYN(false)
                .paymentAmt(insertReservation.paymentAmt())
                .user(insertReservation.user())
                .store(insertReservation.store())
                .build();

        Reservation saveReservation = reservationRepository.save(reservation);

        // 지정한 시간 후에 자동 실행!!
        scheduler.scheduleOnceAfterDelay(10 , "m" , this::autoCancelMethod, saveReservation.getId());
    }

    // 결제 완료
    @Transactional
    public void successReservation(String orderId) {
        Reservation reservation = reservationRepository.findByOrderId(orderId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));
        reservation.updatePaymentYN(true);
    }

    @Transactional
    public void cancelReservation(Long userId , Long storeId , Long reservationId , ReserveRequest.Cancel cancel) {
        // 예약 어떤지?
        Reservation reservation = reservationRepository.findByIdAndStoreId(reservationId , storeId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

        // 본인 예약건인지?
        reservationCheckService.isUserReservation(userId , reservation);

        // 예약을 취소할수 있는 타입인지 검증
        reservationCheckService.canChangeReservationType(reservation , ReservationType.RESERVATION);

        switch (reservation.getStatus()) {
            case RESERVATION:
            case APPLY:
                reservation.updateStatus(ReservationStatus.CANCEL);

                if (reservation.isPaymentYN()) {
                    // PaymentEventListener 결제취소
                    PaymentCancelEvent paymentCancelEvent = new PaymentCancelEvent(reservation.getOrderId() , cancel.cancelReason());
                    eventPublisher.publishEvent(paymentCancelEvent);
                }
                break;
            default:
                throw new ForbiddenException(ResponseCode.CANCEL_FORBIDDEN);
        }
    }

    public Page<ReserveResponse.Infos> getUserReservations(Long userId , ReserveRequest.Parameter parameter) {
        Pageable pageable = PageRequest.of(parameter.page() - 1, parameter.size());
        return reservationRepository.getUserReservations(userId , parameter , pageable);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void autoCancelMethod(Long reservationId) {
        log.info("autoCancelMethod 접근!!");
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

        if (!reservation.isPaymentYN()) {
            // 결제건 취소
            eventPublisher.publishEvent(new PaymentTimeoutCancelEvent(reservation.getOrderId()));
            // 예약건 취소
            reservation.updateStatus(ReservationStatus.CANCEL);
        }
    }

    @Transactional
    @RedisListener(topic = "waiting-poll")
    public void onWaitingPoll(WaitingPoll waitingPoll){
        LocalDateTime at = waitingPoll.createdAt();
        Long userId = waitingPoll.userId();
        Long storeId = waitingPoll.storeId();
        Long num = waitingPoll.waitingNum();

        User user = userRepository.findById(userId).orElseThrow(()-> new NotFoundException(ResponseCode.NOT_FOUND_USER));
        Store store = storeRepository.findById(storeId).orElseThrow(()-> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.APPLY)
                .type(ReservationType.WAIT)
                .user(user)
                .store(store)
                .reservationDate(at.toLocalDate())
                .reservationTime(at.toLocalTime().truncatedTo(TimeUnit.SECONDS.toChronoUnit()))
                .numberPeople(1L)
                .reservationNo(num)
                .menuYN(false)
                .build();

        reservationRepository.save(reservation);
    }
}
