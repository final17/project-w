package com.projectw.domain.store.service;

import com.projectw.domain.store.dto.request.StoreRequestDto;
import com.projectw.domain.store.dto.response.StoreResponseDto;
import com.projectw.security.AuthUser;
import org.springframework.stereotype.Service;


@Service
public interface StoreOwnerService {

    StoreResponseDto createStore(AuthUser authUser, StoreRequestDto storeRequestDto);

    StoreResponseDto getOneStore(AuthUser authUser, Long storeId);

    StoreResponseDto putStore(AuthUser authUser, Long storeId, StoreRequestDto storeRequestDto);

    void deleteStore(AuthUser authUser, Long storeid);

}
