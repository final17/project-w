package com.projectw.domain.crawler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectw.domain.crawler.dto.response.CrawlerResponseDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CrawlerServiceTest {

    private CrawlerService crawlerService;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RBucket rBucket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        crawlerService = new CrawlerService(redissonClient, objectMapper);
        // proxyRequestCounts와 proxyHosts 초기화
        AtomicInteger[] proxyRequestCounts = new AtomicInteger[1];
        proxyRequestCounts[0] = new AtomicInteger(0);
        ReflectionTestUtils.setField(crawlerService, "proxyRequestCounts", proxyRequestCounts);
        ReflectionTestUtils.setField(crawlerService, "proxyHosts", List.of("test-proxy"));
    }

    @Test
    @DisplayName("블로그 검색 캐시 히트 테스트")
    @SuppressWarnings("unchecked")
    void searchNaverBlog_CacheHit() throws JsonProcessingException {
        // given
        String keyword = "테스트";
        int page = 1;
        String cacheKey = "crawler:blog:테스트:page:1";
        String cachedJson = "cached data";
        List<CrawlerResponseDto> expectedResults = List.of(
                new CrawlerResponseDto("제목", "링크", "설명", "썸네일", "날짜")
        );

        when(redissonClient.getBucket(anyString())).thenReturn(rBucket);
        when(rBucket.get()).thenReturn(cachedJson);
        when(objectMapper.readValue(eq(cachedJson), any(TypeReference.class)))
                .thenReturn(expectedResults);

        // when
        List<CrawlerResponseDto> results = crawlerService.searchNaverBlog(keyword, page);

        // then
        assertThat(results).isEqualTo(expectedResults);
        verify(redissonClient).getBucket(cacheKey);
        verify(rBucket).get();
        verify(objectMapper).readValue(eq(cachedJson), any(TypeReference.class));
    }

    @Test
    @DisplayName("뉴스 검색 캐시 히트 테스트")
    @SuppressWarnings("unchecked")
    void searchNaverNews_CacheHit() throws Exception {
        // given
        String keyword = "테스트";
        int page = 1;
        String cacheKey = "crawler:news:테스트:page:1";
        List<CrawlerResponseDto> expectedResults = List.of(
                new CrawlerResponseDto("뉴스제목", "뉴스링크", "뉴스설명", "뉴스썸네일", "2024.01.01")
        );
        String cachedJson = "cached data";

        when(redissonClient.getBucket(anyString())).thenReturn(rBucket);
        when(rBucket.get()).thenReturn(cachedJson);
        when(objectMapper.readValue(eq(cachedJson), any(TypeReference.class)))
                .thenReturn(expectedResults);

        // when
        List<CrawlerResponseDto> results = crawlerService.searchNaverNews(keyword, page);

        // then
        assertThat(results).isEqualTo(expectedResults);
        verify(redissonClient).getBucket(cacheKey);
        verify(rBucket).get();
        verify(objectMapper).readValue(eq(cachedJson), any(TypeReference.class));
    }


    @Test
    @DisplayName("캐시 키 생성 테스트")
    void generateCacheKey() throws Exception {
        // given
        String type = "blog";
        String keyword = "테스트";
        int page = 1;
        String expectedKey = "crawler:blog:테스트:page:1";

        // when
        String actualKey = ReflectionTestUtils.invokeMethod(
                crawlerService,
                "generateCacheKey",
                type, keyword, page
        );

        // then
        assertThat(actualKey).isEqualTo(expectedKey);
    }

    @Test
    @DisplayName("텍스트 추출 헬퍼 메소드 테스트")
    void getFirstNonEmptyText() throws Exception {
        // given
        Element element = Jsoup.parse("""
            <div>
                <span class="first"></span>
                <span class="second">텍스트</span>
                <span class="third">다른텍스트</span>
            </div>
            """).body();

        String[] selectors = {"span.first", "span.second", "span.third"};

        // when
        String result = ReflectionTestUtils.invokeMethod(
                crawlerService,
                "getFirstNonEmptyText",
                element,
                (Object) selectors
        );

        // then
        assertThat(result).isEqualTo("텍스트");
    }
}