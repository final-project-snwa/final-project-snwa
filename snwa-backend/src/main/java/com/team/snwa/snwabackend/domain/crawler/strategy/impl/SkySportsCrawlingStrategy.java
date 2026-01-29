package com.team.snwa.snwabackend.domain.crawler.strategy.impl;

import com.team.snwa.snwabackend.domain.crawler.dto.CrawledArticleDto;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.SourceName;
import com.team.snwa.snwabackend.domain.crawler.strategy.CrawlingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sky Sports 웹사이트(HTML)를 크롤링하는 전략
 * API가 없으므로 뉴스 목록 페이지 HTML을 파싱하여 상세 링크를 얻음
 * @author 허준형
 * @DateOfCreated 2026-01-28
 * @DateOfEdit 2026-01-28
 */
@Slf4j
@Component
public class SkySportsCrawlingStrategy implements CrawlingStrategy {

    @Override
    public SourceName getSourceName() {
        return SourceName.SKY_SPORTS;
    }

    /**
     * @param url ex: "https://www.skysports.com/football/news"
     */
    @Override
    public List<CrawledArticleDto> crawl(String url) {
        log.info("SkySports Crawling started... Target: {}", url);
        List<CrawledArticleDto> resultList = new ArrayList<>();

        try {
            // 봇 아닌 척
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Cache-Control", "max-age=0")
                    .timeout(30000)
                    .ignoreHttpErrors(true)
                    .get();

            // 기사 링크 추출
            Elements links = doc.select("a.sdc-site-tile__headline-link");

            int count = 0;
            for (Element link : links) {
                if (count++ >= 10) break;

                String articleUrl = link.attr("abs:href");
                String title = link.text();

                // 상세 페이지 크롤링
                CrawledArticleDto dto = crawlDetail(articleUrl, title);
                if (dto != null) {
                    resultList.add(dto);
                }

                Thread.sleep(1000);
            }

        } catch (Exception e) {
            log.error("Sky Sports 크롤링 실패: {}", url, e);
            throw new RuntimeException("Sky Sports 크롤링 중 오류", e);
        }

        return resultList;
    }

    /**
     * 상세 페이지 파싱 메서드
     * ESPN의 crawlFullContent()와 비슷한 역할을 하지만,
     * Sky Sports의 HTML 구조(sdc-article-...)에 맞춰져 있음.
     * @author 허준형
     * @DateOfCreated 2026-01-28
     * @DateOfEdit 2026-01-28
     */
    private CrawledArticleDto crawlDetail(String detailUrl, String titleFromList) {

        if (detailUrl.contains("/live-blog/")) {
            return null;
        }

        try {
            // 봇 차단 회피 헤더 설정
            Document doc = Jsoup.connect(detailUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Referer", "https://www.google.com")
                    .timeout(30000)
                    .get();

            String title = "";
            Elements h1 = doc.select("h1.sdc-article-header__title");
            if (!h1.isEmpty()) {
                title = h1.text();
            } else {
                title = titleFromList;
            }

            // 본문 추출
            Elements paragraphs = doc.select("div.sdc-article-body p");

            if (paragraphs.isEmpty()) {
                paragraphs = doc.select("div.sdc-article-body__content p");
            }
            if (paragraphs.isEmpty()) {
                paragraphs = doc.select("div[itemprop='articleBody'] p");
            }
            if (paragraphs.isEmpty()) {
                paragraphs = doc.select("div.sdc-site-layout-sticky-region p");
            }

            // 본문 태그를 전혀 못 찾은 경우 (영상 뉴스 등)
            if (paragraphs.isEmpty()) {
                log.warn("본문 태그를 찾을 수 없음 (구조 변경 또는 영상 뉴스): {}", detailUrl);
                return null;
            }

            String content = paragraphs.text();

            // 내용이 너무 짧으면 의미 없어서 스킵
            if (content.length() < 50) {
                return null;
            }

            // 이미지 추출
            String imageUrl = "";
            Element imgTag = doc.selectFirst("div.sdc-article-image__item img");
            if (imgTag != null) {
                imageUrl = imgTag.attr("src");
            }

            // 기자 이름 추출
            String author = doc.select("div.sdc-article-author__author span[itemprop='name']").text();
            if (author.isEmpty()) author = "Sky Sports Staff";

            return CrawledArticleDto.builder()
                    .title(title)
                    .content(content)
                    .originalUrl(detailUrl)
                    .publisherName("Sky Sports")
                    .authorName(author)
                    .imageUrl(imageUrl)
                    .build();

        } catch (Exception e) {
            // 접속 실패나 파싱 오류 시 경고 로그만 남기고 null 반환 (전체 크롤링 중단 방지)
            log.warn("Sky Sports 상세 파싱 실패 (URL: {}): {}", detailUrl, e.getMessage());
            return null;
        }
    }

}