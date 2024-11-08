package com.projectw.domain.store.dto;

import com.projectw.domain.category.CategoryMapperValue;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.entity.StoreLike;
import com.projectw.domain.user.dto.UserOneResponseDto;

import java.time.LocalTime;

public sealed interface StoreResponse permits StoreResponse.Info, StoreResponse.Like {
    record Info(
            Long id,
            String title,
            String description,
            CategoryMapperValue districtCategory,
            LocalTime openTime,
            LocalTime lastOrder,
            LocalTime closeTime,
            LocalTime turnover,
            Long reservationTableCount,
            Long tableCount,
            String phoneNumber,
            String address,
            Long deposit,
            UserOneResponseDto userOneResponseDto,
            Double latitude,
            Double longitude,
            Long view
    ) implements StoreResponse {
        public Info(Store saveStore) {
            this(
            saveStore.getId(),
            saveStore.getTitle(),
            saveStore.getDescription(),
            new CategoryMapperValue(saveStore.getDistrictCategory()),
            saveStore.getOpenTime(),
            saveStore.getLastOrder(),
            saveStore.getCloseTime(),
            saveStore.getTurnover(),
            saveStore.getReservationTableCount(),
            saveStore.getTableCount(),
            saveStore.getPhoneNumber(),
            saveStore.getAddress(),
            saveStore.getDeposit(),
            new UserOneResponseDto(saveStore.getUser()),
            saveStore.getLatitude(),
            saveStore.getLongitude(),
            saveStore.getView()
            );
        }
    }

    record Like(Long storeId,String storeName, Boolean storeLike) implements StoreResponse {
        public Like(StoreLike storeLike) {
            this(storeLike.getStore().getId(), storeLike.getStore().getTitle(), storeLike.getStoreLike());
        }
    }
}
