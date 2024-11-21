package com.projectw.domain.store.dto;

import com.projectw.domain.category.CategoryMapperValue;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.entity.StoreLike;
import com.projectw.domain.user.dto.UserOneResponseDto;

import java.time.LocalTime;

public sealed interface StoreResponse permits StoreResponse.InfoLike, StoreResponse.Info, StoreResponse.Like, StoreResponse.LikeCount {
    record Info(
            Long id,
            String image,
            String title,
            String description,
            CategoryMapperValue districtCategory, // 여기서 null 체크 필요
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
                    saveStore.getImage(),
                    saveStore.getTitle(),
                    saveStore.getDescription(),
                    saveStore.getDistrictCategory() != null
                            ? new CategoryMapperValue(saveStore.getDistrictCategory())
                            : null, // null 체크 추가
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

    record InfoLike(
            Long id,
            String image,
            String title,
            String description,
            CategoryMapperValue districtCategory, // 여기서 null 체크 필요
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
            Long view,
            Long storeLikeCount
    ) implements StoreResponse {
        public InfoLike(Store saveStore, Long storeLikeCount) {
            this(
                    saveStore.getId(),
                    saveStore.getImage(),
                    saveStore.getTitle(),
                    saveStore.getDescription(),
                    saveStore.getDistrictCategory() != null
                            ? new CategoryMapperValue(saveStore.getDistrictCategory())
                            : null, // null 체크 추가
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
                    saveStore.getView(),
                    storeLikeCount
            );
        }
    }

    record Like(Long storeId,String storeName, Boolean storeLike) implements StoreResponse {
        public Like(StoreLike storeLike) {
            this(storeLike.getStore().getId(), storeLike.getStore().getTitle(), storeLike.getStoreLike());
        }
    }

    record LikeCount(Long storeLikeCount) implements StoreResponse {
        public LikeCount(Long storeLikeCount) {
            this.storeLikeCount = storeLikeCount;
        }
    }
}
