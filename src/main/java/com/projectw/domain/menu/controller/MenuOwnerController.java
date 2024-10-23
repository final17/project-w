package com.projectw.domain.menu.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.menu.dto.request.MenuRequestDto;
import com.projectw.domain.menu.dto.response.MenuResponseDto;
import com.projectw.domain.menu.service.MenuService;
import com.projectw.security.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/owner/stores/{storeId}/menus")
public class MenuOwnerController {

    private final MenuService menuService;

    public MenuOwnerController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<MenuResponseDto>> createMenu(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody MenuRequestDto menuRequestDto,
            @PathVariable("storeId") Long storeId) throws IOException {

        MenuResponseDto menuResponseDto = menuService.createMenu(authUser, menuRequestDto, storeId);

        return ResponseEntity.ok(SuccessResponse.of(menuResponseDto));
    }

    @PutMapping("/{menuId}")
    public ResponseEntity<SuccessResponse<MenuResponseDto>> updateMenu(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody MenuRequestDto requestDto,
            @PathVariable("storeId") Long storeId,
            @PathVariable("menuId") Long menuId) throws IOException {

        MenuResponseDto menuResponseDto = menuService.updateMenu(authUser, requestDto, storeId, menuId);
        return ResponseEntity.ok(SuccessResponse.of(menuResponseDto));
    }

    // 오너가 자신의 가게 메뉴 조회
    @GetMapping
    public ResponseEntity<SuccessResponse<List<MenuResponseDto>>> getOwnerMenus(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("storeId") Long storeId) {

        List<MenuResponseDto> menus = menuService.getOwnerMenus(authUser, storeId);
        return ResponseEntity.ok(SuccessResponse.of(menus));
    }

    // 오너만 메뉴 삭제 가능
    @DeleteMapping("/{menuId}")
    public ResponseEntity<SuccessResponse<Void>> deleteMenu(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("storeId") Long storeId,
            @PathVariable("menuId") Long menuId) {

        menuService.deleteMenu(authUser, storeId, menuId);
        return ResponseEntity.ok(SuccessResponse.empty());
    }
}