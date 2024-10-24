package com.projectw.domain.allergy.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AllergyRequestDto {
    private String name;         // 알레르기 이름
    private String description;  // 알레르기 설명
}