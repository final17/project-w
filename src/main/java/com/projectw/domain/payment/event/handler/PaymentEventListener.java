package com.projectw.domain.payment.event.handler;

import com.projectw.domain.payment.event.PaymentEvent;
import com.projectw.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final PaymentService paymentService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Payment event received: {}", event);
//        paymentService.getPayments(event.getUserId() , event.getStoreId() , event.getRevervationId() , event.getPaymentKey() , event.getOrderId() , event.getAmount());
    }
}
