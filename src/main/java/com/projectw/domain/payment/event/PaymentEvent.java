package com.projectw.domain.payment.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PaymentEvent {
    private final Long userId;
    private final Long storeId;
    private final Long revervationId;
    private final String paymentKey;
    private final String orderId;
    private final String amount;
}
