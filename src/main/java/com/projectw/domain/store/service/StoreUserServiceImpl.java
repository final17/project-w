package com.projectw.domain.store.service;

import com.projectw.domain.store.dto.response.StoreResponseDto;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreUserServiceImpl implements StoreUserService {

    private final StoreRepository storeRepository;

    @Override
    public Page<StoreResponseDto> getAllStore(AuthUser authUser, Pageable pageable) {
        Page<Store> allStore = storeRepository.findAll(pageable);
        return allStore.map(StoreResponseDto::new);
    }

    @Override
    public StoreResponseDto getOneStore(AuthUser authUser, Long storeId) {
        Store findStore = storeRepository.findById(storeId).orElseThrow();
        return new StoreResponseDto(findStore);
    }

    @Override
    public Page<StoreResponseDto> serchStoreName(AuthUser authUser, String storeName, Pageable pageable) {
        Page<Store> storeList = storeRepository.findAllByTitle(pageable, storeName);

        return storeList.map(StoreResponseDto::new);
    }

}
