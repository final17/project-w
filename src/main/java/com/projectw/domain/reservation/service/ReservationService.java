package com.projectw.domain.reservation.service;

import com.projectw.common.annotations.RedisLock;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.ForbiddenException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.common.exceptions.UnauthorizedException;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.projectw.domain.reservation.exception.DuplicateReservationException;
import com.projectw.domain.reservation.exception.InvalidReservationTimeException;
import com.projectw.domain.reservation.exception.StoreNotOpenException;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Transactional
    @RedisLock("#waiting")
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
        if (reservationRepository.existsByUserIdAndStoreIdAndTypeAndStatus(userId , storeId , ReservationType.WAIT , ReservationStatus.APPLY)) {
            throw new DuplicateReservationException(ResponseCode.DUPLICATE_RESERVATION);
        }

        // 예약번호 채번하기
        LocalDate now = LocalDate.now();
        Long reservationNo = reservationRepository.findMaxReservationDate(ReservationType.WAIT , now);
        
        // 예약 Entity 만들기
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.APPLY)
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
    @RedisLock("#reservation")
    public void saveReservation(Long userId , Long storeId , ReserveRequest.Reservation reserv) {
        // 현재시간대를 기준으로 예약 가능한 시간 값이 들어왔는지 검증
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        if (reserv.reservationDate().isBefore(nowDate)) {
            throw new InvalidReservationTimeException(ResponseCode.INVALID_RESERVATION_TIME);
        } else if(reserv.reservationDate().equals(nowDate)) {
            if(reserv.reservationTime().isBefore(nowTime)) {
                throw new InvalidReservationTimeException(ResponseCode.INVALID_RESERVATION_TIME);
            }
        }

        // 유저 있는지?
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_USER));
        // 식당이 있는지?
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        // 본인 식당에 예약 가능한지?
        if (store.getUser().equals(user)) {
            throw new UnauthorizedException(ResponseCode.UNAUTHORIZED_STORE_RESERVATION);
        }

        // 두번 예약 불가
        if (reservationRepository.existsByUserIdAndStoreIdAndTypeAndStatus(userId , storeId , ReservationType.RESERVATION , ReservationStatus.RESERVATION)) {
            throw new DuplicateReservationException(ResponseCode.DUPLICATE_RESERVATION);
        }

        // 예약 가능한 시간대인지? 어떻게 처리할지 고민할 것
        // 1. 들고 온 값을 분으로 변환
        int minutes = reserv.reservationTime().getHour() * 60 + reserv.reservationTime().getMinute();

        int baseMinutes = store.getTurnover().getHour() * 60 + store.getTurnover().getMinute();

        // 2. turnover 값을 나누기 - 예외처리!!
        if (minutes % baseMinutes != 0) {
            log.error("예약 불가능한 시간대로 값이 들어왔음!!");
            throw new InvalidReservationTimeException(ResponseCode.INVALID_RESERVATION_TIME);
        }

        // 3. 예약테이블에 개수 조회
        List<ReservationStatus> statusList = Arrays.asList(ReservationStatus.CANCEL, ReservationStatus.AUTOMATIC_CANCEL);
        long remainder = reservationRepository.countReservationByDate(ReservationType.RESERVATION , statusList , reserv.reservationDate() , reserv.reservationTime());

        // 4. 예약개수 비교 작업 - 예외처리!!
        if (store.getReservationTableCount() <= remainder) {
            log.error("해당 시간대에 예약수가 꽉 참!!");
            throw new InvalidReservationTimeException(ResponseCode.INVALID_RESERVATION_TIME);
        }

        // 예약번호 채번하기
        LocalDate now = LocalDate.now();
        Long reservationNo = reservationRepository.findMaxReservationDate(ReservationType.RESERVATION , now);

        // 예약 Entity 만들기
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.RESERVATION)
                .type(ReservationType.RESERVATION)
                .menuYN(reserv.menuYN())
                .numberPeople(reserv.numberPeople())
                .reservationNo(reservationNo)
                .reservationDate(reserv.reservationDate())
                .reservationTime(reserv.reservationTime())
                .user(user)
                .store(store)
                .build();

        reservationRepository.save(reservation);
    }

    @Transactional
    public void reservationCancelReservation(Long userId , Long reservationId) {
        // 예약 어떤지?
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

        // 본인 예약건인지?
        if (!reservation.getUser().getId().equals(userId)) {
            throw new UnauthorizedException(ResponseCode.UNAUTHORIZED_RESERVATION);
        }

        if (reservation.getType() != ReservationType.RESERVATION) {
            throw new ForbiddenException(ResponseCode.CANCEL_FORBIDDEN);
        }

        switch (reservation.getStatus()) {
            case RESERVATION:
            case APPLY:
                reservation.updateStatus(ReservationStatus.CANCEL);
                break;
            default:
                throw new ForbiddenException(ResponseCode.CANCEL_FORBIDDEN);
        }

    }

    @Transactional
    public void waitCancelReservation(Long userId , Long reservationId) {
        // 예약 어떤지?
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

        // 본인 예약건인지?
        if (!reservation.getUser().getId().equals(userId)) {
            throw new UnauthorizedException(ResponseCode.UNAUTHORIZED_RESERVATION);
        }

        if (reservation.getType() != ReservationType.WAIT) {
            throw new ForbiddenException(ResponseCode.CANCEL_FORBIDDEN);
        }

        switch (reservation.getStatus()) {
            case APPLY:
                reservation.updateStatus(ReservationStatus.CANCEL);
                break;
            default:
                throw new ForbiddenException(ResponseCode.CANCEL_FORBIDDEN);
        }
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

    public Page<ReserveResponse.Infos> getOnwerReservations(Long userId , ReserveRequest.Parameter parameter) {
        Pageable pageable = PageRequest.of(parameter.page() - 1, parameter.size());
        return reservationRepository.getOwnerReservations(userId , parameter , pageable);
    }

    public Page<ReserveResponse.Infos> getUserReservations(Long userId , ReserveRequest.Parameter parameter) {
        Pageable pageable = PageRequest.of(parameter.page() - 1, parameter.size());
        return reservationRepository.getUserReservations(userId , parameter , pageable);
    }

    public ReserveResponse.Info getReservation(Long userId , Long reservationId) {
        return reservationRepository.getReservation(userId , reservationId);
    }
}
