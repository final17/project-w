package com.projectw.domain.payment.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PaymentCancelEvent {
    private final String orderId;
    private final String cancelReason;
}
