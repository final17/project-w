package com.projectw.domain.reservation.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.ForbiddenException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.common.exceptions.UnauthorizedException;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.projectw.domain.reservation.exception.DuplicateReservationException;
import com.projectw.domain.reservation.exception.StoreNotOpenException;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public void saveWait(Long userId , Long storeId , ReserveRequest.Wait wait) {
        // 유저 있는지?
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_USER));
        // 식당이 있는지?
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        // 본인이 본인 식당에 예약 가능 한지?
        if (store.getUser().equals(user)) {
            throw new UnauthorizedException(ResponseCode.UNAUTHORIZED_STORE_RESERVATION);
        }

        // 웨이팅 예약 가능한 시간대인지?
        if (storeRepository.countStoresOpenNow(storeId) < 1) {
            throw new StoreNotOpenException(ResponseCode.STORE_NOT_OPEN);
        }

        // 같은 아이디로 두번 예약 불가
        if (reservationRepository.existsByUserIdAndStoreIdAndTypeAndStatus(userId , storeId , ReservationType.WAIT , ReservationStatus.RESERVATION)) {
            throw new DuplicateReservationException(ResponseCode.DUPLICATE_RESERVATION);
        }

        // 예약번호 채번하기
        LocalDate now = LocalDate.now();
        Long reservationNo = reservationRepository.findMaxReservationDate(ReservationType.WAIT , now);
        
        // 예약 Entity 만들기
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.RESERVATION)
                .type(ReservationType.WAIT)
                .menuYN(wait.menuYN())
                .numberPeople(wait.numberPeople())
                .reservationNo(reservationNo)
                .reservationDate(LocalDate.now())
                .reservationTime(LocalTime.now())
                .user(user)
                .store(store)
                .build();

        reservationRepository.save(reservation);
    }

    @Transactional
    public void saveReservation(Long userId , Long storeId , ReserveRequest.Reservation reserv) {
        // 유저 있는지?
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_USER));
        // 식당이 있는지?
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        // 본인 식당에 예약 가능한지?
        if (store.getUser().equals(user)) {
            throw new UnauthorizedException(ResponseCode.UNAUTHORIZED_STORE_RESERVATION);
        }

        // 예약 가능한 시간대인지? 어떻게 처리할지 고민할 것


        // 두번 예약 불가
        if (reservationRepository.existsByUserIdAndStoreIdAndTypeAndStatus(userId , storeId , ReservationType.RESERVATION , ReservationStatus.RESERVATION)) {
            throw new DuplicateReservationException(ResponseCode.DUPLICATE_RESERVATION);
        }

        // 예약번호 채번하기
        LocalDate now = LocalDate.now();
        Long reservationNo = reservationRepository.findMaxReservationDate(ReservationType.RESERVATION , now);

        // 예약 Entity 만들기
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.RESERVATION)
                .type(ReservationType.WAIT)
                .menuYN(reserv.menuYN())
                .numberPeople(reserv.numberPeople())
                .reservationNo(reservationNo)
                .reservationDate(LocalDate.parse(reserv.reservationDate()))
                .reservationTime(LocalTime.parse(reserv.reservationTime()))
                .user(user)
                .store(store)
                .build();

        reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservation(Long userId , Long reservationId) {
        // 예약 어떤지?
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

        // 본인 예약건인지?
        if (!reservation.getUser().getId().equals(userId)) {
            throw new UnauthorizedException(ResponseCode.UNAUTHORIZED_RESERVATION);
        }

        // 취소 가능한지?
        if (reservation.getStatus() != ReservationStatus.RESERVATION) {
            throw new ForbiddenException(ResponseCode.CANCEL_FORBIDDEN);
        }

        reservation.updateStatus(ReservationStatus.CANCEL);
    }

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
        if (reservation.getStatus() != ReservationStatus.RESERVATION) {
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

    public void getOnwerReservation(Long userId , ReserveRequest.Parameter parameter) {

    }

    public void getUserReservation(Long userId , ReserveRequest.Parameter parameter) {

    }
}
