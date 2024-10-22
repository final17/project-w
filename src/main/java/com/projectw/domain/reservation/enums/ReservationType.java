package com.projectw.domain.reservation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ReservationType {
    WAIT(Status.WAIT),                  // 웨이팅
    RESERVATION(Status.RESERVATION);    // 예약

    private final String status;

    public static ReservationType of(String status) {
        return Arrays.stream(ReservationType.values())
                .filter(r -> r.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 ReservationType"));
    }

    public static class Status {
        public static final String WAIT = "WAIT";
        public static final String RESERVATION = "RESERVATION";
    }
}
