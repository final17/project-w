package com.projectw.domain.menu.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.menu.dto.response.MenuResponseDto;
import com.projectw.domain.menu.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/stores/{storeId}/menus")
public class MenuUserController {

    private MenuService menuService;
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
    public ResponseEntity<SuccessResponse<MenuResponseDto>> likeMenu(@PathVariable Long menuId) {
        MenuResponseDto response = menuService.likeMenu(menuId);
        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    // 조회수 증가 API
    @GetMapping("/{menuId}/view")
    public ResponseEntity<SuccessResponse<MenuResponseDto>> viewMenu(@PathVariable Long menuId) {
        MenuResponseDto response = menuService.viewMenu(menuId);
        return ResponseEntity.ok(SuccessResponse.of(response));
    }
}
