package com.projectw.domain.allergy.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public sealed interface AllergyUpdateRequestDto permits AllergyUpdateRequestDto.Update {

    record Update(
            @NotEmpty(message = "알레르기 ID 목록은 비어 있을 수 없습니다.") Set<Long> allergyIds
    ) implements AllergyUpdateRequestDto { }
}