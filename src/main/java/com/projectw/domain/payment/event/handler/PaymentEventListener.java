package com.projectw.domain.payment.event.handler;

import com.projectw.domain.payment.event.PaymentCancelEvent;
import com.projectw.domain.payment.event.PaymentTimeoutCancelEvent;
import com.projectw.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePaymentCancelEvent(PaymentCancelEvent event) throws Exception {
        log.info("handlePaymentCancelEvent");
        paymentService.cancel(event.getOrderId() , event.getCancelReason());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePaymentTimeoutCancelEvent(PaymentTimeoutCancelEvent event) {
        log.info("handlePaymentTimeoutCancelEvent");
        paymentService.timeoutCancel(event.getOrderId());
    }

}
