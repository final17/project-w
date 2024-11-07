package com.projectw.domain.waiting.dto;

import lombok.Getter;

import java.util.Set;

@Getter
public class StoreRank {

    private String message;
    private Set<StoreInfo> topStores;

    public StoreRank(String message, Set<StoreInfo> topStores) {
        this.message = message;
        this.topStores = topStores;
    }

    @Getter
    public static class StoreInfo {
        private String storeId;
        private String storeTitle;

        public StoreInfo(String storeId, String storeTitle) {
            this.storeId = storeId;
            this.storeTitle = storeTitle;
        }
    }
}
