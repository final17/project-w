package com.projectw.domain.reservation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ReservationStatus {
    RESERVATION(Status.RESERVATION),
    CANCEL(Status.CANCEL),
    AUTOMATIC_CANCEL(Status.AUTOMATIC_CANCEL),
    APPLY(Status.APPLY),
    COMPLETE(Status.COMPLETE);

    private final String status;

    public static ReservationStatus of(String status) {
        return Arrays.stream(ReservationStatus.values())
                .filter(r -> r.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 ReservationStatus"));
    }

    public static class Status {
        public static final String RESERVATION = "RESERVATION";             //예약 - 예약시스템에서만
        public static final String CANCEL = "CANCEL";                       //취소 - 사용자가 취소
        public static final String AUTOMATIC_CANCEL = "AUTOMATIC_CANCEL";   //자동취소(스케줄러) - wait에서 20분내로
        public static final String APPLY = "APPLY";                         //승인 - 가게에서 승인한것
        public static final String COMPLETE = "COMPLETE";                   //완료 - 방문완료
    }
}
