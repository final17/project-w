package com.projectw.domain.store.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.store.dto.request.StoreRequestDto;
import com.projectw.domain.store.dto.response.StoreResponseDto;
import com.projectw.domain.store.service.StoreOwnerService;
import com.projectw.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/owner/stores")
@RequiredArgsConstructor
public class StoreOwnerController {

    private final StoreOwnerService storeOwnerService;

    // 음식점 생성
    @PostMapping()
    public ResponseEntity<SuccessResponse<StoreResponseDto>> createStore(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody StoreRequestDto storeRequestDto
    ) {
        StoreResponseDto storeResponseDto = storeOwnerService.createStore(authUser, storeRequestDto);
        return ResponseEntity.ok(SuccessResponse.of(storeResponseDto));
    }

    // 음식점 단건 조회
    @GetMapping("/{storeId}")
    public ResponseEntity<SuccessResponse<StoreResponseDto>> getOneStore(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("storeId") Long storeId
    ) {
        StoreResponseDto storeResponseDto = storeOwnerService.getOneStore(authUser, storeId);
        return ResponseEntity.ok(SuccessResponse.of(storeResponseDto));
    }

    // 음식점 정보 수정
    @PutMapping("/{storeId}")
    public ResponseEntity<SuccessResponse<StoreResponseDto>> putStore(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("storeId") Long storeId,
            @RequestBody StoreRequestDto storeRequestDto
    ) {
        StoreResponseDto storeResponseDto = storeOwnerService.putStore(authUser, storeId, storeRequestDto);
        return ResponseEntity.ok(SuccessResponse.of(storeResponseDto));
    }

    // 음식점 삭제
    @DeleteMapping("/{storeId}")
    public ResponseEntity<SuccessResponse<String>> deleteStore(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("storeId") Long storeid
    ) {
        storeOwnerService.deleteStore(authUser, storeid);
        return ResponseEntity.ok(SuccessResponse.of(""));
    }

    // 예약 조회

    // 웨이팅 조회

    // 예약 상태 변경(예약 거절, 예절 수락)

}
