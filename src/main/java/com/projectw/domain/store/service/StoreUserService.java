package com.projectw.domain.store.service;

import com.projectw.domain.store.dto.StoreResponse;
import com.projectw.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface StoreUserService {
    Page<StoreResponse.Info> getAllStore(AuthUser authUser, Pageable pageable);

    StoreResponse.Info getOneStore(AuthUser authUser, Long storeId);

    Page<StoreResponse.Info> serchStoreName(AuthUser authUser, String storeName, Pageable pageable);

    StoreResponse.Like likeStore(AuthUser authUser, Long storeId);

    Page<StoreResponse.Like> getLikeStore(AuthUser authUser, Pageable pageable);
}
