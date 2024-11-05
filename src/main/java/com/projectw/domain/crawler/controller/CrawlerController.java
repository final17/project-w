package com.projectw.domain.crawler.controller;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.domain.crawler.dto.response.CrawlerResponseDto;
import com.projectw.domain.crawler.service.CrawlerService;
import com.projectw.domain.crawler.service.KeywordGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/crawler")
public class CrawlerController {

    private final CrawlerService crawlerService;
    private final KeywordGeneratorService keywordGeneratorService;

    /**
     * 네이버 블로그 검색
     */
    @GetMapping("/blog")
    public ResponseEntity<SuccessResponse<List<CrawlerResponseDto>>> searchBlog(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page
    ) {
        List<CrawlerResponseDto> results = crawlerService.searchNaverBlog(keyword, page);
        return ResponseEntity.ok(SuccessResponse.of(results));
    }

    /**
     * 네이버 뉴스 검색
     */
    @GetMapping("/news")
    public ResponseEntity<SuccessResponse<List<CrawlerResponseDto>>> searchNews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page
    ) {
        List<CrawlerResponseDto> results = crawlerService.searchNaverNews(keyword, page);
        return ResponseEntity.ok(SuccessResponse.of(results));
    }

    @GetMapping("/keywords/{storeId}")
    public ResponseEntity<SuccessResponse<List<String>>> generateKeywords(@PathVariable Long storeId) {
        List<String> keywords = keywordGeneratorService.generateKeywords(storeId);
        return ResponseEntity.ok(SuccessResponse.of(keywords));
    }
}