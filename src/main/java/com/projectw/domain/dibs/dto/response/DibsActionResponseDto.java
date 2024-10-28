package com.projectw.domain.dibs.dto.response;

import lombok.Getter;

@Getter
public class DibsActionResponseDto {
    private String message; // 성공 또는 삭제 메시지
    private DibsResponseDto dibsResponse; // 찜이 추가된 경우 반환될 DibsResponseDto

    public DibsActionResponseDto(String message, DibsResponseDto dibsResponse) {
        this.message = message;
        this.dibsResponse = dibsResponse;
    }
}