package com.projectw.domain.payment.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PaymentTimeoutCancelEvent {
    private final String orderId;
}
