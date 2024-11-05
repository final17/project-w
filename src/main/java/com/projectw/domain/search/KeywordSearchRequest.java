package com.projectw.domain.search;

import java.util.List;

public sealed interface KeywordSearchRequest permits KeywordSearchRequest.Search, KeywordSearchRequest.Filter{
    record Search(
            Filter filters,
            int size,
            String keyword
    ) implements KeywordSearchRequest { }

    record Filter(
            List<String> districtCategories,
            List<String> cuisineCategories
    ) implements KeywordSearchRequest { }
}
