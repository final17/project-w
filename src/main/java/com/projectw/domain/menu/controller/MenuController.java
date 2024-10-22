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

@RestController
@RequestMapping("/api/v1/owner/stores/{storeId}/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<MenuResponseDto>> createMenu(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody MenuRequestDto menuRequestDto,
            @PathVariable Long storeId) throws IOException {

        MenuResponseDto menuResponseDto = menuService.createMenu(authUser, menuRequestDto, storeId);

        return ResponseEntity.ok(SuccessResponse.of(menuResponseDto));
    }

    @PutMapping("/{menuId}")
    public ResponseEntity<SuccessResponse<MenuResponseDto>> updateMenu(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody MenuRequestDto requestDto,
            @PathVariable Long storeId,
            @PathVariable Long menuId) throws IOException {

        MenuResponseDto menuResponseDto = menuService.updateMenu(authUser, requestDto, storeId, menuId);
        return ResponseEntity.ok(SuccessResponse.of(menuResponseDto));
    }
}