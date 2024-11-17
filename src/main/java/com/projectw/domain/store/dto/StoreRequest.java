package com.projectw.domain.store.dto;

import java.time.LocalTime;

public sealed interface StoreRequest permits StoreRequest.Category, StoreRequest.Create {
    record Create (
            String title,
            String districtCategoryCode,
            String description,
            LocalTime openTime,
            LocalTime lastOrder,
            LocalTime closeTime,
            LocalTime turnover,
            Long reservationTableCount,
            Long tableCount,
            String phoneNumber,
            String address,
            Long deposit,
            Double latitude,
            Double longitude
    ) implements StoreRequest{ }

    record Category(String categoryCode) implements StoreRequest { }
}
