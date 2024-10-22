package com.projectw.domain.store.service;

import com.projectw.domain.store.dto.response.StoreResponseDto;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<StoreResponseDto> getAllStore(AuthUser authUser) {
        List<Store> allStore = storeRepository.findAll();
        return allStore.stream().map(StoreResponseDto::new).toList();
    }

    @Override
    public StoreResponseDto getOneStore(AuthUser authUser, Long storeId) {
        Store findStore = storeRepository.findById(storeId).orElseThrow();
        return new StoreResponseDto(findStore);
    }

}
