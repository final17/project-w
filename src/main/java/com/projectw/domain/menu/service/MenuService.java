package com.projectw.domain.menu.service;

import com.projectw.common.config.S3Service;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.domain.menu.dto.MenuRequestDto;
import com.projectw.domain.menu.dto.MenuResponseDto;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final S3Service s3Service;

    // 메뉴 생성 (ROLE_OWNER 권한만 가능)
    public MenuResponseDto createMenu(AuthUser authUser, MenuRequestDto requestDto, Long storeId) throws IOException {
        // 사용자 권한 확인 (ROLE_OWNER만 메뉴 생성 가능)
        if (authUser.getRole() != UserRole.ROLE_OWNER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN); // 권한 없을 때 예외 발생
        }

        // 스토어 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("스토어를 찾을 수 없습니다."));

        // S3에 이미지 업로드 (이미지가 있는 경우에만)
        String menuImageUrl = null;
        if (requestDto.getMenuImage() != null) {
            menuImageUrl = s3Service.uploadFile(requestDto.getMenuImage());
        }

        Menu menu = new Menu(
                requestDto.getName(),
                requestDto.getPrice(),
                requestDto.getAllergies(),
                menuImageUrl,
                store
        );
        Menu savedMenu = menuRepository.save(menu);

        // MenuResponseDTO로 반환
        return new MenuResponseDto(
                savedMenu.getId(),
                savedMenu.getName(),
                savedMenu.getPrice(),
                savedMenu.getAllergies(),
                savedMenu.getMenuImageUrl()
        );
    }

    // 메뉴 수정 (ROLE_OWNER 권한만 가능)
    public MenuResponseDto updateMenu(AuthUser authUser, MenuRequestDto requestDto, Long storeId, Long menuId) throws IOException {
        // 사용자 권한 확인 (ROLE_OWNER만 메뉴 수정 가능)
        if (authUser.getRole() != UserRole.ROLE_OWNER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN); // 권한 없을 때 예외 발생
        }

        // 메뉴 및 스토어 조회
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException(ResponseCode.NOT_FOUND_STORE.getMessage()));

        // S3에 이미지 업로드 (이미지가 변경되었을 경우)
        String menuImageUrl = requestDto.getMenuImage() != null ? s3Service.uploadFile(requestDto.getMenuImage()) : menu.getMenuImageUrl();

        // 메뉴 수정
        menu.updateMenu(requestDto.getName(), requestDto.getPrice(), requestDto.getAllergies(), menuImageUrl);
        Menu updatedMenu = menuRepository.save(menu);

        // MenuResponseDTO로 반환
        return new MenuResponseDto(
                updatedMenu.getId(),
                updatedMenu.getName(),
                updatedMenu.getPrice(),
                updatedMenu.getAllergies(),
                updatedMenu.getMenuImageUrl()
        );
    }

    // 모든 유저가 특정 가게의 메뉴 조회
    public List<MenuResponseDto> getMenusByStore(Long storeId) {
        // 스토어 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스토어를 찾을 수 없습니다."));

        // 해당 가게의 메뉴 조회
        List<Menu> menus = menuRepository.findAllByStore(store);

        return menus.stream().map(menu ->
                new MenuResponseDto(
                        menu.getId(),
                        menu.getName(),
                        menu.getPrice(),
                        menu.getAllergies(),
                        menu.getMenuImageUrl()
                )
        ).collect(Collectors.toList());
    }

    // 오너가 자신의 가게 메뉴만 조회
    public List<MenuResponseDto> getOwnerMenus(AuthUser authUser, Long storeId) {
        if (authUser.getRole() != UserRole.ROLE_OWNER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("스토어를 찾을 수 없습니다."));

        // 해당 스토어의 메뉴만 조회
        List<Menu> menus = menuRepository.findAllByStore(store);

        return menus.stream().map(menu ->
                new MenuResponseDto(
                        menu.getId(),
                        menu.getName(),
                        menu.getPrice(),
                        menu.getAllergies(),
                        menu.getMenuImageUrl()
                )
        ).collect(Collectors.toList());
    }
}
