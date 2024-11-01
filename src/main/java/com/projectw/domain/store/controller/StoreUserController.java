package com.projectw.domain.store.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.store.dto.response.StoreResponseDto;
import com.projectw.domain.store.service.StoreUserService;
import com.projectw.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/stores")
@RequiredArgsConstructor
public class StoreUserController {

    private final StoreUserService storeUserService;

    @GetMapping
    public ResponseEntity<SuccessResponse<Page<StoreResponseDto>>> getAllStore(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StoreResponseDto> StoreResponseDtoList = storeUserService.getAllStore(authUser, pageable);
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

    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<Page<StoreResponseDto>>> serchStoreName(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String storeName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StoreResponseDto> storeResponseDto = storeUserService.serchStoreName(authUser, storeName, pageable);

        return ResponseEntity.ok(SuccessResponse.of(storeResponseDto));
    }
}
