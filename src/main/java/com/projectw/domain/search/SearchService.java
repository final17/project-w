package com.projectw.domain.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggester;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import com.projectw.common.annotations.ExecutionTimeLog;
import com.projectw.domain.category.DistrictCategory;
import com.projectw.domain.category.HierarchicalCategoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient elasticsearchClient;

    @ExecutionTimeLog
    public KeywordSearchResponse.AutoComplete autoComplete(String keyword) {

        String index = "stores";

        CompletionSuggester completion = new CompletionSuggester.Builder().field("title.completion").size(10).build();
        Suggester build = new Suggester.Builder()
                .text(keyword)
                .suggesters("title-suggest", field->field.completion(completion))
                .build();

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(index)
                .suggest(build)
                .build();

        // 요청 실행
        try {
            SearchResponse<StoreDoc> result  = elasticsearchClient.search(searchRequest, StoreDoc.class);
            // 추천 결과 출력
            List<CompletionSuggestOption<StoreDoc>> options = result
                    .suggest()
                    .get("title-suggest")
                    .get(0)
                    .completion()
                    .options();

            List<StoreDoc> list = options
                    .stream()
                    .map(CompletionSuggestOption::source)
                    .toList();
            return new KeywordSearchResponse.AutoComplete(keyword, list);

        } catch (IOException e) {
            log.error(e.getMessage());

            return new KeywordSearchResponse.AutoComplete(keyword, List.of());
        }

    }

    /**
     * 가게명 검색 후 결과가 없으면 통합검색
     */
    @ExecutionTimeLog
     public KeywordSearchResponse.Search intergratedSearch(KeywordSearchRequest.Search search) {

        log.info("가게명 검색!!");
        log.info("keyword: {}", search.keyword());
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder()
                .must(m -> m.match(t -> t
                        .field("full_text")
                        .query(search.keyword())
                        .operator(Operator.And)))
                .should(s -> s.match(t -> t
                        .field("title")
                        .query(search.keyword())
                        .boost(1.5f)))
                .should(s -> s.term(t -> t
                        .field("title.keyword")
                        .value(search.keyword())
                        .boost(2.0f)));


        if(search.filters() != null){

             if(search.filters().districtCategories() != null && !search.filters().districtCategories().isEmpty()) {
                 List<FieldValue> list = search.filters().districtCategories().stream().map(x -> FieldValue.of(HierarchicalCategoryUtils.codeToCategory(DistrictCategory.class, x).getPath())).toList();

                 Query query = TermsQuery.of(x -> x.field("district_category.keyword").terms(t -> t.value(list)))._toQuery();
                 boolBuilder.filter(query);
             }
         }
        BoolQuery query = boolBuilder.build();
         SearchRequest searchRequest = SearchRequest.of(s -> s
                 .index("stores")
                 .size(30)
                 .query(query._toQuery()));

         try{
             SearchResponse<StoreDoc> result = elasticsearchClient.search(searchRequest, StoreDoc.class);
             log.info("::: Elasticsearch ::: 검색 키워드:{} 요청 사이즈:{} 검색된 사이즈: {}", search.keyword(), search.size(), result.hits().hits().size());

             return KeywordSearchResponse.Search.builder()
                     .keyword(search.keyword())
                     .stores(result.hits().hits().stream().map(Hit::source).toList())
                     .build();

         } catch (IOException e) {
             log.error(e.getMessage());
             return KeywordSearchResponse.Search.builder()
                     .keyword(search.keyword())
                     .stores(List.of())
                     .build();
         }
     }
}