package com.projectw.domain.store.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.store.dto.StoreResponse;
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
    public ResponseEntity<SuccessResponse<Page<StoreResponse.Info>>> getAllStore(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StoreResponse.Info> StoreResponseDtoList = storeUserService.getAllStore(authUser, pageable);
        return ResponseEntity.ok(SuccessResponse.of(StoreResponseDtoList));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<SuccessResponse<StoreResponse.Info>> getOneStore(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("storeId") Long storeId
    ) {
        StoreResponse.Info storeResponseDto = storeUserService.getOneStore(authUser, storeId);
        return ResponseEntity.ok(SuccessResponse.of(storeResponseDto));
    }

    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<Page<StoreResponse.Info>>> serchStoreName(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String storeName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StoreResponse.Info> storeResponseDto = storeUserService.serchStoreName(authUser, storeName, pageable);

        return ResponseEntity.ok(SuccessResponse.of(storeResponseDto));
    }

    @PatchMapping("/{storeId}")
    public ResponseEntity<SuccessResponse<StoreResponse.Like>> likeStore(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("storeId") Long storeId
    ) {
        StoreResponse.Like storeLikeResposeDto = storeUserService.likeStore(authUser, storeId);
        return ResponseEntity.ok(SuccessResponse.of(storeLikeResposeDto));
    }

    // 좋아하는 store 목록 조회
    @GetMapping("/likes")
    public ResponseEntity<SuccessResponse<Page<StoreResponse.Like>>> getLikeStore(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StoreResponse.Like> storeLikeResposeDto = storeUserService.getLikeStore(authUser, pageable);
        return ResponseEntity.ok(SuccessResponse.of(storeLikeResposeDto));
    }
}
