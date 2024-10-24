package com.projectw.domain.allergy.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AllergyUpdateRequestDto {
    @NotEmpty(message = "알레르기 ID 목록은 비어 있을 수 없습니다.")
    private Set<Long> allergyIds;
}