package com.projectw.domain.store.dto;

import com.projectw.domain.store.entity.StoreLike;
import lombok.Getter;

@Getter
public class StoreLikeResposeDto {
    private Long storeId;
    private String storeName;
    private boolean storeLike;

    public StoreLikeResposeDto(StoreLike storeLike) {
        this.storeId = storeLike.getStore().getId();
        this.storeName = storeLike.getStore().getTitle();
        this.storeLike = storeLike.getStoreLike();
    }
}
