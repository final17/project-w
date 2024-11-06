package com.projectw.domain.reservation.dto;


import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public sealed interface ReserveRequest permits
        ReserveRequest.InsertReservation ,
        ReserveRequest.Cancel ,
        ReserveRequest.AddCart ,
        ReserveRequest.UpdateCart ,
        ReserveRequest.RemoveCart ,
        ReserveRequest.Parameter {

    record InsertReservation(
            @NotBlank(message = "주문번호는 필수입니다.")
            String orderId,
            @NotBlank(message = "예약날짜는 필수입니다.")
            LocalDate reservationDate,
            @NotBlank(message = "예약시간은 필수입니다.")
            LocalTime reservationTime,
            @NotNull(message = "입장인원값은 필수입니다.")
            Long numberPeople,
            @NotNull(message = "예약금은 필수입니다.")
            Long paymentAmt,
            User user,
            Store store
    ) implements ReserveRequest {}

    record Cancel(
            @NotBlank(message = "취소사유는 필수입니다.")
            String cancelReason
    ) implements ReserveRequest {}

    record AddCart(
            List<ReserveMenuRequest.Menu> menus
    ) implements ReserveRequest {}

    record UpdateCart(
            @NotNull(message = "메뉴 식별키는 필수입니다.")
            Long menuId,
            @NotNull(message = "메뉴 개수는 필수입니다.")
            Long menuCnt
    ) implements ReserveRequest {}

    record RemoveCart(
            @NotNull(message = "메뉴 식별키는 필수입니다.")
            Long menuId
    ) implements ReserveRequest {}

    record Parameter(
            ReservationType type,
            ReservationStatus status,
            LocalDate startDt,
            LocalDate endDt,
            Integer page,
            Integer size
    ) implements ReserveRequest {
        public Parameter {
            if (Objects.isNull(page)) page = 1;
            if (Objects.isNull(size)) size = 10;
        }
    }
}
