package com.projectw.domain.store.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.store.dto.response.StoreResponseDto;
import com.projectw.domain.store.service.StoreUserService;
import com.projectw.security.AuthUser;
import com.sun.net.httpserver.Authenticator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/stores")
@RequiredArgsConstructor
public class StoreUserController {

    private final StoreUserService storeUserService;

    @GetMapping()
    public ResponseEntity<SuccessResponse<List<StoreResponseDto>>> getAllStore(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        List<StoreResponseDto> StoreResponseDtoList = storeUserService.getAllStore(authUser);
        return ResponseEntity.ok(SuccessResponse.of(StoreResponseDtoList));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<SuccessResponse<StoreResponseDto>> getOneStore(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("storeId") Long storeId
    ) {
        StoreResponseDto storeResponseDto = storeUserService.getOneStore(authUser, storeId);
        return ResponseEntity.ok(SuccessResponse.of(storeResponseDto));
    }
}
