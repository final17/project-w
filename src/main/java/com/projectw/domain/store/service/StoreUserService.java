package com.projectw.domain.store.service;

import com.projectw.domain.store.dto.response.StoreResponseDto;
import com.projectw.security.AuthUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StoreUserService {
    List<StoreResponseDto> getAllStore(AuthUser authUser);

    StoreResponseDto getOneStore(AuthUser authUser, Long storeId);
}
