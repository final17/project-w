package com.projectw.domain.menu.service;

import com.projectw.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MenuService {

    private final MenuRepository menuRepository;
}
