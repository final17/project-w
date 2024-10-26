package com.projectw.domain.dibs.dto.response;

import com.projectw.domain.dibs.entity.Dibs;
import lombok.Getter;

@Getter
public class DibsResponseDto {
    private Long dibsId;
    private Long storeId;
    private String storeTitle;

    public DibsResponseDto(Dibs dibs) {
        this.dibsId = dibs.getId();
        this.storeId = dibs.getStore().getId();
        this.storeTitle = dibs.getStore().getTitle();
    }
}
