package com.projectw.domain.store.service;

import com.projectw.domain.store.dto.response.StoreLikeResposeDto;
import com.projectw.domain.store.dto.response.StoreResponseDto;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.entity.StoreLike;
import com.projectw.domain.store.repository.StoreLikeRepository;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreUserServiceImpl implements StoreUserService {

    private final StoreRepository storeRepository;
    private final StoreLikeRepository storeLikeRepository;
    private final UserRepository userRepository;

    @Override
    public Page<StoreResponseDto> getAllStore(AuthUser authUser, Pageable pageable) {
        Page<Store> allStore = storeRepository.findAll(pageable);
        return allStore.map(StoreResponseDto::new);
    }

    @Override
    @Transactional
    public StoreResponseDto getOneStore(AuthUser authUser, Long storeId) {
        Store findStore = storeRepository.findById(storeId).orElseThrow(()-> new IllegalArgumentException("음식점을 찾을 수 없습니다."));
        findStore.addView();
        return new StoreResponseDto(findStore);
    }

    @Override
    public Page<StoreResponseDto> serchStoreName(AuthUser authUser, String storeName, Pageable pageable) {
        Page<Store> storeList = storeRepository.findAllByTitle(pageable, storeName);

        return storeList.map(StoreResponseDto::new);
    }

    @Override
    @Transactional
    public StoreLikeResposeDto likeStore(AuthUser authUser, Long storeId) {
        Store findStore = storeRepository.findById(storeId).orElseThrow(() -> new IllegalArgumentException("음식점을 찾을 수 없습니다."));
        User findUser = userRepository.findById(authUser.getUserId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        StoreLike storeLike = storeLikeRepository.findByStoreIdAndUserId(findStore.getId(), findUser.getId());

        if (storeLike == null) {
            StoreLike newStoreLike = new StoreLike(findUser, findStore);
            storeLike = storeLikeRepository.save(newStoreLike);
        } else {
            storeLike.changeLike();
        }

        return new StoreLikeResposeDto(storeLike);
    }

    @Override
    public Page<StoreLikeResposeDto> getLikeStore(AuthUser authUser, Pageable pageable) {
        Page<StoreLike> storeLikes = storeLikeRepository.findAllByUserId(authUser.getUserId(), pageable);
        return storeLikes.map(StoreLikeResposeDto::new);
    }
}
