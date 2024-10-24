package com.projectw.domain.reservation.service;

import com.projectw.common.annotations.RedisListener;
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
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.domain.waiting.dto.WaitingPoll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

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
    public void reservationCancelReservation(Long userId , Long storeId , Long reservationId) {
        // 예약 어떤지?
        Reservation reservation = reservationRepository.findByIdAndStoreId(reservationId , storeId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

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

    public Page<ReserveResponse.Infos> getUserReservations(Long userId , ReserveRequest.Parameter parameter) {
        Pageable pageable = PageRequest.of(parameter.page() - 1, parameter.size());
        return reservationRepository.getUserReservations(userId , parameter , pageable);
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
