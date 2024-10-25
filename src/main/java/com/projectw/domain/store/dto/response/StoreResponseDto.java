package com.projectw.domain.store.dto.response;

import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.dto.UserOneResponseDto;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class StoreResponseDto {

    private Long id;
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
    private UserOneResponseDto userOneResponseDto;

    public StoreResponseDto(Store saveStore) {
        id = saveStore.getId();
        title = saveStore.getTitle();
        description = saveStore.getDescription();
        openTime = saveStore.getOpenTime();
        lastOrder = saveStore.getLastOrder();
        closeTime = saveStore.getCloseTime();
        turnover = saveStore.getTurnover();
        reservationTableCount = saveStore.getReservationTableCount();
        tableCount = saveStore.getTableCount();
        phoneNumber = saveStore.getPhoneNumber();
        address = saveStore.getAddress();
        deposit = saveStore.getDeposit();
        userOneResponseDto = new UserOneResponseDto(saveStore.getUser());
    }
}
