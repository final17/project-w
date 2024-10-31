package com.projectw.domain.menu.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.domain.menu.dto.response.MenuResponseDto;
import com.projectw.domain.menu.service.MenuService;
import com.projectw.security.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/stores/{storeId}/menus")
public class MenuUserController {

    private final MenuService menuService;

    public MenuUserController(MenuService menuService) {
        this.menuService = menuService;
    }

    // 모든 유저가 특정 가게의 메뉴 조회
    @GetMapping
    public ResponseEntity<SuccessResponse<List<MenuResponseDto>>> getMenusByStore(@PathVariable Long storeId) {
        List<MenuResponseDto> menus = menuService.getMenusByStore(storeId);
        return ResponseEntity.ok(SuccessResponse.of(menus));
    }

    // 좋아요 증가 API
    @PostMapping("/{menuId}/like")
    public ResponseEntity<SuccessResponse<MenuResponseDto>> toggleLikeMenu(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long menuId) {

        // 유저 권한 확인
        if (authUser == null || authUser.getRole() == UserRole.ROLE_OWNER) {
            throw new AccessDeniedException(ResponseCode.FORBIDDEN);
        }

        // 좋아요 토글 서비스 호출
        MenuResponseDto menuResponseDto = menuService.likeMenu(menuId, authUser.toUserEntity());
        return ResponseEntity.ok(SuccessResponse.of(menuResponseDto));
    }

    // 조회수 증가 API
    @GetMapping("/{menuId}/view")
    public ResponseEntity<SuccessResponse<MenuResponseDto>> viewMenu(@PathVariable Long menuId) {
        MenuResponseDto response = menuService.viewMenu(menuId);
        return ResponseEntity.ok(SuccessResponse.of(response));
    }
}
