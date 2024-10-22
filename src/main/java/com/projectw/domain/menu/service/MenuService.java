package com.projectw.domain.menu.service;

import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
}
