package com.projectw.domain.store.service;

import com.projectw.domain.store.dto.response.StoreResponseDto;
import com.projectw.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StoreUserService {
    Page<StoreResponseDto> getAllStore(AuthUser authUser, Pageable pageable);

    StoreResponseDto getOneStore(AuthUser authUser, Long storeId);

    Page<StoreResponseDto> serchStoreName(AuthUser authUser, String storeName, Pageable pageable);
}
