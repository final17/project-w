package com.projectw.domain.store.dto.request;

import lombok.Getter;

import java.time.LocalTime;

@Getter
public class StoreRequestDto {
    private String title;
    private String description;
    private LocalTime openTime;
    private LocalTime lastOrder;
    private LocalTime closeTime;
    private LocalTime turnover;
    private Long reservationTableCount;
    private Long tableCount;
    private String phoneNumber;
    private String address;
    private Long deposit;
    private Double latitude;
    private Double longitude;
}
