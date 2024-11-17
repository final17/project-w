package com.projectw.domain.reservation.event;

import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@RequiredArgsConstructor
@Getter
public class ReservationInsertEvent {
    private final String orderId;
    private final LocalDate date;
    private final LocalTime time;
    private final Long numberPeople;
    private final Long paymentAmt;
    private final User user;
    private final Store store;
}
