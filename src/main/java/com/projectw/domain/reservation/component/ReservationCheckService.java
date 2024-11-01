package com.projectw.domain.reservation.component;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.ForbiddenException;
import com.projectw.common.exceptions.InvalidRequestException;
import com.projectw.common.exceptions.UnauthorizedException;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.projectw.domain.reservation.exception.InvalidReservationTimeException;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReservationCheckService {

    private final ReservationRepository reservationRepository;

    /**
     * 날짜와 시간 값을 기준으로 현재 시간과 비교하여 값이 유효한지 검증
     * */
    public void isReservationDateValid(LocalDate date , LocalTime time) {
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        if (date.isBefore(nowDate)) {
            throw new InvalidReservationTimeException(ResponseCode.INVALID_RESERVATION_TIME);
        } else if(date.equals(nowDate)) {
            if(time.isBefore(nowTime)) {
                throw new InvalidReservationTimeException(ResponseCode.INVALID_RESERVATION_TIME);
            }
        }
    }

    /**
     * 식당에 설정된 예약금과 들어온 예약금액이 같은지 검증
     * */
    public void validateDepositAmount(Store store, Long amount) {
        if (!store.getDeposit().equals(amount)) {
            throw new InvalidRequestException(ResponseCode.INVALID_AMOUNT);
        }
    }

    /**
     * 본인 식당에는 예약 불가!!
     * */
    public void validateUserAuthorization(Store store , User user) {
        if (store.getUser().equals(user)) {
            throw new UnauthorizedException(ResponseCode.UNAUTHORIZED_STORE_RESERVATION);
        }
    }

    /**
     * 예약 가능한 시간대인지 검증
     * */
    public void validateReservationTime(Store store , LocalTime time) {
        int minutes = time.getHour() * 60 + time.getMinute();
        int baseMinutes = store.getTurnover().getHour() * 60 + store.getTurnover().getMinute();

        if (minutes % baseMinutes != 0) {
            log.error("예약 불가능한 시간대로 값이 들어왔음!!");
            throw new InvalidReservationTimeException(ResponseCode.INVALID_RESERVATION_TIME);
        }
    }

    /**
     * 예약가능수 비교 검증
     * */
    public boolean checkReservationCapacity(Store store , LocalDate date , LocalTime time) {
        List<ReservationStatus> statusList = Arrays.asList(ReservationStatus.CANCEL , ReservationStatus.COMPLETE);
        long remainder = reservationRepository.countReservationByDate(ReservationType.RESERVATION , statusList , date , time);
        return !(store.getReservationTableCount() <= remainder);
    }

    /**
     * 본인 예약건인지 검증(유저 기준)
     * */
    public void isUserReservation(Long userId , Reservation reservation) {
        if (!reservation.getUser().getId().equals(userId)) {
            throw new UnauthorizedException(ResponseCode.UNAUTHORIZED_RESERVATION);
        }
    }

    /**
     * 본인 예약건인지 검증(OWNER 기준)
     * */
    public void isOwnerReservation(Long userId , Reservation reservation) {
        if (!reservation.getStore().getUser().getId().equals(userId)) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }
    }

    /**
     * 해당 타입가 아니라면 변경 불가!!
     * */
    public void canChangeReservationType(Reservation reservation , ReservationType type) {
        if (reservation.getType() != type) {
            throw new ForbiddenException(ResponseCode.CANCEL_FORBIDDEN);
        }
    }

    /**
     * 해당 상태가 아니라면 변경 불가!!
     * */
    public void canChangeReservationStatus(Reservation reservation , ReservationStatus status , ResponseCode responseCode) {
        if (reservation.getStatus() != status) {
            throw new ForbiddenException(responseCode);
        }
    }

}
