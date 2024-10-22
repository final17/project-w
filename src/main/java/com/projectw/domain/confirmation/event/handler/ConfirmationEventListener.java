package com.projectw.domain.confirmation.event.handler;

import com.projectw.domain.confirmation.event.ConfirmationEvent;
import com.projectw.domain.confirmation.service.ConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConfirmationEventListener {
    private final ConfirmationService confirmationService;

    /**
     * 웨이팅 및 예약 완료시 전송 이벤트
     * */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConfirmationEvent(ConfirmationEvent event) {
        confirmationService.save();
    }
}
