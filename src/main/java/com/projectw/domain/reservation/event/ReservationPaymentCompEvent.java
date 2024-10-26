package com.projectw.domain.reservation.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ReservationPaymentCompEvent {
    private final String orderId;
}
