package com.projectw.domain.crawler.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CrawlerResponseDto {
    private String title;
    private String link;
    private String description;
    private String thumbnail;
    private String date;

}
