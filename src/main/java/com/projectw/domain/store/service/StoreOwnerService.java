package com.projectw.domain.store.service;

import com.projectw.domain.store.dto.StoreRequest;
import com.projectw.domain.store.dto.StoreResponse;
import com.projectw.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public interface StoreOwnerService {

    StoreResponse.Info createStore(AuthUser authUser, StoreRequest.Create storeRequestDto, MultipartFile image);

    StoreResponse.Info getOneStore(AuthUser authUser, Long storeId);

    StoreResponse.Info putStore(AuthUser authUser, Long storeId, StoreRequest.Create storeRequestDto, MultipartFile image);

    StoreResponse.Info updateCategory(AuthUser authUser, Long storeId, StoreRequest.Category updateCategory);
    void deleteStore(AuthUser authUser, Long storeid);

    Page<StoreResponse.Info> getMyStores(AuthUser authUser, Pageable pageable);


}
