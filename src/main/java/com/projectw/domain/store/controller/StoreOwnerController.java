package com.projectw.domain.store.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.store.dto.request.StoreRequestDto;
import com.projectw.domain.store.dto.response.StoreResponseDto;
import com.projectw.domain.store.service.StoreOwnerService;
import com.projectw.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner/stores")
@RequiredArgsConstructor
public class StoreOwnerController {

    private final StoreOwnerService storeOwnerService;

    // 음식점 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<StoreResponseDto>> createStore(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart(name = "image", required = false) MultipartFile image,
            @RequestPart StoreRequestDto storeRequestDto
    ) {
        validateCoordinates(storeRequestDto.getLatitude(), storeRequestDto.getLongitude());
        StoreResponseDto storeResponseDto = storeOwnerService.createStore(authUser, storeRequestDto, image);
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
    @PutMapping(value = "/{storeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<StoreResponseDto>> putStore(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("storeId") Long storeId,
            @RequestPart(name = "image", required = false) MultipartFile image,
            @RequestPart StoreRequestDto storeRequestDto
    ) {
        StoreResponseDto storeResponseDto = storeOwnerService.putStore(authUser, storeId, storeRequestDto, image);
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

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("위도와 경도는 필수 입력값입니다.");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("위도는 -90도에서 90도 사이여야 합니다.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("경도는 -180도에서 180도 사이여야 합니다.");
        }
    }



    // 예약 조회

    // 웨이팅 조회

    // 예약 상태 변경(예약 거절, 예절 수락)

}
