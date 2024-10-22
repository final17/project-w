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

//        // 사용자가 이 스토어의 주인인지 확인
//        if (!store.getOwnerId().equals(authUser.getUserId())) {
//            throw new AccessDeniedException(ResponseCode.FORBIDDEN); // 스토어 주인이 아닌 경우 예외 발생
//        }

        // S3에 이미지 업로드
        String menuImageUrl = s3Service.uploadFile(requestDto.getMenuImage());

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
}
