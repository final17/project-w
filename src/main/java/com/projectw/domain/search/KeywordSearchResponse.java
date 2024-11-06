package com.projectw.domain.search;

import com.projectw.domain.store.dto.response.CategoryMapperValue;
import lombok.Builder;

import java.util.List;

public sealed interface KeywordSearchResponse permits KeywordSearchResponse.Search, KeywordSearchResponse.KeywordSearchResult, KeywordSearchResponse.Filter, KeywordSearchResponse.AutoComplete{

    @Builder
    record Search(
            String keyword,
            List<CategoryMapperValue> filters,
            List<StoreDoc> stores
    ) implements KeywordSearchResponse {}

    record AutoComplete (
            String keyword,
            List<StoreDoc> stores) implements KeywordSearchResponse { }

    record Filter(
            List<CategoryMapperValue> district,
            List<CategoryMapperValue> legalDistrict,
            List<CategoryMapperValue> cuisine) implements  KeywordSearchResponse {}

    record KeywordSearchResult(List<CategoryMapperValue> filters) implements KeywordSearchResponse {}
}
