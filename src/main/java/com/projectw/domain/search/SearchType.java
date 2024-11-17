package com.projectw.domain.search;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SearchType {
    INTEGRATED_SEARCH("integrated"),
    STORE_NAME_SEARCH("store");
    private final String type;
}
