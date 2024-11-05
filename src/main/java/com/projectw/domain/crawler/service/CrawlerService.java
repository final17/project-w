package com.projectw.domain.crawler.service;

import com.projectw.domain.crawler.dto.response.CrawlerResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerService {

    /**
     * 네이버 블로그 검색
     */
    public List<CrawlerResponseDto> searchNaverBlog(String keyword, int page) {

        List<CrawlerResponseDto> results = new ArrayList<>();
        String url = String.format(
                "https://search.naver.com/search.naver?ssc=tab.blog.all&sm=tab_jum&query=%s&start=%d",
                URLEncoder.encode(keyword, StandardCharsets.UTF_8),
                (page - 1) * 10 + 1
        );

        log.info("Crawling URL: {}", url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Cache-Control", "no-cache")
                    .header("Cookie", "NNB=VVEU745SXPUFE")
                    .header("sec-ch-ua", "\"Chromium\";v=\"118\", \"Google Chrome\";v=\"118\", \"Not=A?Brand\";v=\"99\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"macOS\"")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .referrer("https://search.naver.com")
                    .timeout(5000)
                    .get();

            // 여러 CSS 선택자 시도
            Elements posts = doc.select("div.total_area");
            if (posts.isEmpty()) {
                posts = doc.select("li.bx");
            }
            if (posts.isEmpty()) {
                posts = doc.select("div.view_wrap");
            }

            log.info("Found {} posts", posts.size());

            for (Element post : posts) {
                try {
                    // 여러 가능한 선택자 시도
                    String title = getFirstNonEmptyText(post,
                            "a.title_link",
                            "div.title_area a",
                            "a.title"
                    );

                    String link = getFirstNonEmptyAttr(post,
                            "a.title_link",
                            "div.title_area a",
                            "a.title",
                            "href"
                    );

                    String description = getFirstNonEmptyText(post,
                            "div.dsc",
                            "div.detail_box",
                            "div.text"
                    );

                    String thumbnail = getFirstNonEmptyAttr(post,
                            "img.thumb",
                            "div.detail_thumb img",
                            "img",
                            "src"
                    );

                    String date = getFirstNonEmptyText(post,
                            "span.date",
                            "span.sub_time",
                            "span.time"
                    );

                    if (!title.isEmpty()) {
                        log.debug("Parsed post - Title: {}, Link: {}", title, link);
                        results.add(new CrawlerResponseDto(title, link, description, thumbnail, date));
                    }

                } catch (Exception e) {
                    log.error("Failed to parse blog post", e);
                }
            }

            if (results.isEmpty()) {
                // 디버깅을 위해 HTML 구조 출력
                log.debug("HTML Structure:\n{}", doc.html());
            }

        } catch (IOException e) {
            log.error("Failed to crawl blog posts for keyword: {}", keyword, e);
            throw new RuntimeException("블로그 검색 중 오류 발생", e);
        }

        return results;
    }

    // 여러 선택자 중 첫 번째로 찾아지는 텍스트 반환
    private String getFirstNonEmptyText(Element element, String... selectors) {
        for (String selector : selectors) {
            String text = element.select(selector).text().trim();
            if (!text.isEmpty()) {
                return text;
            }
        }
        return "";
    }

    // 여러 선택자 중 첫 번째로 찾아지는 속성 값 반환
    private String getFirstNonEmptyAttr(Element element, String... selectors) {
        String attributeName = selectors[selectors.length - 1];
        for (int i = 0; i < selectors.length - 1; i++) {
            String attr = element.select(selectors[i]).attr(attributeName).trim();
            if (!attr.isEmpty()) {
                return attr;
            }
        }
        return "";
    }

    /**
     * 네이버 뉴스 검색
     */
    public List<CrawlerResponseDto> searchNaverNews(String keyword, int page) {
        List<CrawlerResponseDto> results = new ArrayList<>();
        String url = String.format(
                "https://search.naver.com/search.naver?where=news&query=%s&start=%d",
                keyword,
                (page - 1) * 10 + 1
        );

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();

            Elements articles = doc.select("div.news_wrap");

            for (Element article : articles) {
                String title = article.select("a.news_tit").text();
                String link = article.select("a.news_tit").attr("href");
                String description = article.select("div.news_dsc").text();
                String thumbnail = article.select("img.thumb").attr("src");
                String date = article.select("span.info").first().text();

                results.add(new CrawlerResponseDto(title, link, description, thumbnail, date));
            }

        } catch (IOException e) {
            throw new RuntimeException("뉴스 검색 중 오류 발생", e);
        }

        return results;
    }
}
