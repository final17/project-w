package com.projectw.domain.menu.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.menu.dto.response.MenuResponseDto;
import com.projectw.domain.menu.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/stores/{storeId}/menus")
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
}
