package com.projectw.domain.store.service;

import com.projectw.domain.store.dto.request.StoreRequestDto;
import com.projectw.domain.store.dto.response.StoreResponseDto;
import com.projectw.security.AuthUser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Service
public interface StoreOwnerService {

    StoreResponseDto createStore(AuthUser authUser, StoreRequestDto storeRequestDto, MultipartFile image);

    StoreResponseDto getOneStore(AuthUser authUser, Long storeId);

    StoreResponseDto putStore(AuthUser authUser, Long storeId, StoreRequestDto storeRequestDto, MultipartFile image);

    void deleteStore(AuthUser authUser, Long storeid);

}
