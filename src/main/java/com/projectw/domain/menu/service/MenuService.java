package com.projectw.domain.menu.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.domain.allergy.entity.Allergy;
import com.projectw.domain.allergy.repository.AllergyRepository;
import com.projectw.domain.menu.dto.request.MenuRequestDto;
import com.projectw.domain.menu.dto.response.MenuResponseDto;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final AllergyRepository allergyRepository;

    // 메뉴 생성 (ROLE_OWNER 권한만 가능)
    public MenuResponseDto createMenu(AuthUser authUser, MenuRequestDto requestDto, Long storeId) throws IOException {
        // 사용자 권한 확인 (ROLE_OWNER만 메뉴 생성 가능)
        if (authUser.getRole() != UserRole.ROLE_OWNER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN); // 권한 없을 때 예외 발생
        }

        // 스토어 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("스토어를 찾을 수 없습니다."));

        // 알레르기 정보 가져오기
        Set<Allergy> allergies = allergyRepository.findAllById(requestDto.getAllergyIds())
                .stream().collect(Collectors.toSet());

        // 메뉴 생성 및 저장
        Menu menu = new Menu(
                requestDto.getName(),
                requestDto.getPrice(),
                store,
                allergies
        );
        Menu savedMenu = menuRepository.save(menu);

        return new MenuResponseDto(
                savedMenu.getId(),
                savedMenu.getName(),
                savedMenu.getPrice(),
                savedMenu.getAllergies().stream().map(Allergy::getName).collect(Collectors.toSet()));
    }

    // 메뉴 수정 (ROLE_OWNER 권한만 가능)
    public MenuResponseDto updateMenu(AuthUser authUser, MenuRequestDto requestDto, Long storeId, Long menuId) throws IOException {
        // 사용자 권한 확인 (ROLE_OWNER만 메뉴 수정 가능)
        if (authUser.getRole() != UserRole.ROLE_OWNER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN); // 권한 없을 때 예외 발생
        }

        // 메뉴 및 스토어 조회
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_MENU.getMessage()));

        // 알레르기 정보 업데이트
        Set<Allergy> allergies = allergyRepository.findAllById(requestDto.getAllergyIds())
                .stream().collect(Collectors.toSet());
        menu.updateMenu(
                requestDto.getName(),
                requestDto.getPrice(),
                allergies
        );

        Menu updatedMenu = menuRepository.save(menu);

        return new MenuResponseDto(
                updatedMenu.getId(),
                updatedMenu.getName(),
                updatedMenu.getPrice(),
                updatedMenu.getAllergies().stream().map(Allergy::getName).collect(Collectors.toSet()));
    }

    // 모든 유저가 특정 가게의 메뉴 조회
    public List<MenuResponseDto> getMenusByStore(Long storeId) {
        // 스토어 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스토어를 찾을 수 없습니다."));

        // 해당 가게의 메뉴 조회 (isDeleted가 false인 메뉴만 조회)
        List<Menu> menus = menuRepository.findAllByStoreAndIsDeletedFalse(store);

        return menus.stream().map(menu -> {
            Set<String> allergyNames = menu.getAllergies().stream()
                    .map(Allergy::getName)
                    .collect(Collectors.toSet());
            return new MenuResponseDto(
                    menu.getId(),
                    menu.getName(),
                    menu.getPrice(),
                    allergyNames
            );
        }).collect(Collectors.toList());
    }

    // 오너가 자신의 가게 메뉴만 조회
    public List<MenuResponseDto> getOwnerMenus(AuthUser authUser, Long storeId) {
        if (authUser.getRole() != UserRole.ROLE_OWNER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_STORE.getMessage()));

        // 해당 스토어의 메뉴만 조회 (isDeleted가 false인 메뉴만)
        List<Menu> menus = menuRepository.findAllByStoreAndIsDeletedFalse(store);

        return menus.stream().map(menu -> {
            Set<String> allergyNames = menu.getAllergies().stream()
                    .map(Allergy::getName)
                    .collect(Collectors.toSet());
            return new MenuResponseDto(
                    menu.getId(),
                    menu.getName(),
                    menu.getPrice(),
                    allergyNames
            );
        }).collect(Collectors.toList());
    }

    // 메뉴 삭제 (ROLE_OWNER 권한만 가능)
    public void deleteMenu(AuthUser authUser, Long storeId, Long menuId) {
        // 오너 권한 확인
        if (authUser.getRole() != UserRole.ROLE_OWNER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN); // 권한 없음 예외 처리
        }

        // 스토어 및 메뉴 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_STORE.getMessage()));
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_MENU.getMessage()));

        // 메뉴가 해당 스토어에 속해 있는지 확인
        if (!menu.getStore().getId().equals(store.getId())) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN); // 스토어 주인이 아닌 경우 예외 처리
        }

        // 메뉴 삭제
        menu.deleteMenu();
        menuRepository.save(menu);
    }
}