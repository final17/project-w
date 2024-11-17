package com.projectw.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Status {
    PENDING(Status.StatusType.PENDING),
    COMPLETED(Status.StatusType.COMPLETED),
    CANCELLED(Status.StatusType.CANCELLED),
    REFUNDED(Status.StatusType.REFUNDED);

    private final String status;

    public static Status of(String value) {
        return Arrays.stream(Status.values())
                .filter(r -> r.status.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 PaymentStatus"));
    }

    public static class StatusType {
        public static final String PENDING = "PENDING";
        public static final String COMPLETED = "COMPLETED";
        public static final String CANCELLED = "CANCELLED";
        public static final String REFUNDED = "REFUNDED";
    }
}
