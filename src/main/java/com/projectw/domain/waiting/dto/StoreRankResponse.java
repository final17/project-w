package com.projectw.domain.waiting.dto;

import lombok.Getter;

import java.util.Set;

@Getter
public class StoreRankResponse {

    private String message;
    private Set<String> topStores;

    public StoreRankResponse(String message) {
        this.message = message;
    }

    public StoreRankResponse(String message, Set<String> topStores) {
        this.message = message;
        this.topStores = topStores;
    }
}
