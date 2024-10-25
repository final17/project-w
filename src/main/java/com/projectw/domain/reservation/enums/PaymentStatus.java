package com.projectw.domain.reservation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    WAIT(PaymentStatus.Status.WAIT),
    COMP(PaymentStatus.Status.COMP);

    private final String status;

    public static PaymentStatus of(String status) {
        return Arrays.stream(PaymentStatus.values())
                .filter(r -> r.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 PaymentStatus"));
    }

    public static class Status {
        public static final String WAIT = "WAIT";  //대기
        public static final String COMP = "COMP";  //완료
    }
}
