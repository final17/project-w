package com.projectw.domain.waiting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum WaitingStatus {
    RESERVATION(Status.RESERVATION),
    CANCEL(Status.CANCEL),
    AUTOMATIC_CANCEL(Status.AUTOMATIC_CANCEL),
    COMPLETE(Status.COMPLETE);

    private final String status;

    public static WaitingStatus of(String role) {
        return Arrays.stream(WaitingStatus.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 WaitingStatus"));
    }

    public static class Status {
        public static final String RESERVATION = "RESERVATION";
        public static final String CANCEL = "CANCEL";
        public static final String AUTOMATIC_CANCEL = "AUTOMATIC_CANCEL";
        public static final String COMPLETE = "COMPLETE";
    }
}
