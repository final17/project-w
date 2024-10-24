package com.projectw.domain.allergy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AllergyResponseDto {
    private Long id;          // 알레르기 ID
    private String name;      // 알레르기 이름
    private String description; // 알레르기 설명
}