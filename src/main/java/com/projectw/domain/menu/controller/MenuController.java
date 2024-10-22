package com.projectw.domain.menu.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.menu.dto.MenuRequestDto;
import com.projectw.domain.menu.dto.MenuResponseDto;
import com.projectw.domain.menu.service.MenuService;
import com.projectw.security.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping("/owner/stores/{storeId}/menus")
    public ResponseEntity<SuccessResponse<MenuResponseDto>> createMenu(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody MenuRequestDto menuRequestDto,
            @PathVariable Long storeId) throws IOException {

        MenuResponseDto menuResponseDto = menuService.createMenu(authUser, menuRequestDto, storeId);

        return ResponseEntity.ok(SuccessResponse.of(menuResponseDto));
    }

    @PutMapping("/owner/stores/{storeId}/menus/{menuId}")
    public ResponseEntity<SuccessResponse<MenuResponseDto>> updateMenu(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody MenuRequestDto requestDto,
            @PathVariable Long storeId,
            @PathVariable Long menuId) throws IOException {

        MenuResponseDto menuResponseDto = menuService.updateMenu(authUser, requestDto, storeId, menuId);
        return ResponseEntity.ok(SuccessResponse.of(menuResponseDto));
    }

    // 모든 유저가 특정 가게의 메뉴 조회
    @GetMapping("/user/stores/{storeId}/menus")
    public ResponseEntity<SuccessResponse<List<MenuResponseDto>>> getMenusByStore(@PathVariable Long storeId) {
        List<MenuResponseDto> menus = menuService.getMenusByStore(storeId);
        return ResponseEntity.ok(SuccessResponse.of(menus));
    }

    // 오너가 자신의 가게 메뉴 조회
    @GetMapping("/owner/stores/{storeId}/menus")
    public ResponseEntity<SuccessResponse<List<MenuResponseDto>>> getOwnerMenus(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId) {

        List<MenuResponseDto> menus = menuService.getOwnerMenus(authUser, storeId);
        return ResponseEntity.ok(SuccessResponse.of(menus));
    }

    // 오너만 메뉴 삭제 가능
    @DeleteMapping("/owner/stores/{storeId}/menus/{menuId}")
    public ResponseEntity<SuccessResponse<Void>> deleteMenu(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @PathVariable Long menuId) {

        menuService.deleteMenu(authUser, storeId, menuId);
        return ResponseEntity.ok(SuccessResponse.empty());
    }
}