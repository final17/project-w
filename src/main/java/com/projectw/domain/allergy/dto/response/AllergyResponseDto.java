package com.projectw.domain.allergy.dto.response;

public sealed interface AllergyResponseDto permits AllergyResponseDto.Basic {

    record Basic(Long id, String name, String description) implements AllergyResponseDto { }
}