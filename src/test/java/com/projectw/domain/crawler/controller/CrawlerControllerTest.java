package com.projectw.domain.crawler.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.crawler.dto.response.CrawlerResponseDto;
import com.projectw.domain.crawler.service.CrawlerService;
import com.projectw.domain.crawler.service.KeywordGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CrawlerControllerTest {

    private CrawlerController crawlerController;
    private CrawlerService crawlerService;
    private KeywordGeneratorService keywordGeneratorService;

    @BeforeEach
    void setUp() {
        crawlerService = mock(CrawlerService.class);
        keywordGeneratorService = mock(KeywordGeneratorService.class);
        crawlerController = new CrawlerController(crawlerService, keywordGeneratorService);
    }

    @Test
    @DisplayName("네이버 블로그 검색 테스트")
    void searchBlog_Success() {
        // given
        String keyword = "테스트";
        int page = 1;
        List<CrawlerResponseDto> expectedResults = List.of(
                new CrawlerResponseDto("테스트 제목", "http://test.com", "테스트 설명", "test.jpg", "2024.01.01")
        );

        when(crawlerService.searchNaverBlog(keyword, page)).thenReturn(expectedResults);

        // when
        ResponseEntity<SuccessResponse<List<CrawlerResponseDto>>> response =
                crawlerController.searchBlog(keyword, page);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEqualTo(expectedResults);
        verify(crawlerService).searchNaverBlog(keyword, page);
    }

    @Test
    @DisplayName("네이버 뉴스 검색 테스트")
    void searchNews_Success() {
        // given
        String keyword = "테스트";
        int page = 1;
        List<CrawlerResponseDto> expectedResults = List.of(
                new CrawlerResponseDto("뉴스 제목", "http://news.com", "뉴스 설명", "news.jpg", "2024.01.01")
        );

        when(crawlerService.searchNaverNews(keyword, page)).thenReturn(expectedResults);

        // when
        ResponseEntity<SuccessResponse<List<CrawlerResponseDto>>> response =
                crawlerController.searchNews(keyword, page);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEqualTo(expectedResults);
        verify(crawlerService).searchNaverNews(keyword, page);
    }

    @Test
    @DisplayName("키워드 생성 테스트")
    void generateKeywords_Success() {
        // given
        Long storeId = 1L;
        List<String> expectedKeywords = List.of("맛집1", "맛집2", "맛집3");

        when(keywordGeneratorService.generateKeywords(storeId)).thenReturn(expectedKeywords);

        // when
        ResponseEntity<SuccessResponse<List<String>>> response =
                crawlerController.generateKeywords(storeId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEqualTo(expectedKeywords);
        verify(keywordGeneratorService).generateKeywords(storeId);
    }
}