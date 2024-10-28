package com.projectw.domain.dibs.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DibsRequestDto {
    private Long storeId;

    public DibsRequestDto(Long storeId) {
        this.storeId = storeId;
    }
}
