package com.team.snwa.snwabackend.domain.crawler.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.snwa.snwabackend.domain.crawler.dto.CrawledArticleDto;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.SourceName;
import com.team.snwa.snwabackend.domain.crawler.strategy.CrawlingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ESPN 사이트(NBA News API)를 대상으로 동작하는 크롤링 전략 구현체
 * JSON API를 통해 기사 목록을 조회한 후, 각 기사의 상세 페이지를 Jsoup으로 스크래핑하여 본문을 수집함
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2026-01-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EspnCrawlingStrategy implements CrawlingStrategy {

    private final ObjectMapper objectMapper;
    private static final String ESPN_NBA_API_URL = "http://site.api.espn.com/apis/site/v2/sports/basketball/nba/news";

    /**
     * 지정된 타겟 URL(API)에 접속하여 기사 리스트를 수집하고 파싱을 수행함
     * 차단 방지를 위해 상세 페이지 접근 시 딜레이(Thread.sleep)를 적용함
     *
     * @param url 크롤링할 타겟 URL (내부적으로 상수를 우선 사용할 수 있음)
     * @return 수집 완료된 기사 DTO 리스트
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-26
     */
    @Override
    public List<CrawledArticleDto> crawl(String url) {
        log.info("ESPN Crawling started... Target: {}", ESPN_NBA_API_URL);
        List<CrawledArticleDto> resultList = new ArrayList<>();

        try {
            //
            String jsonResponse = Jsoup.connect(ESPN_NBA_API_URL)
                    .ignoreContentType(true)
                    .method(Connection.Method.GET)
                    .execute()
                    .body();

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode articlesNode = rootNode.path("articles");

            if (articlesNode.isArray()) {
                for (JsonNode article : articlesNode) {
                    CrawledArticleDto dto = parseArticleAndGetDetail(article);
                    if (dto != null) {
                        resultList.add(dto);
                    }

                    // 차단 방지 텀 주기
                    Thread.sleep(1000);
                }
            }

        } catch (Exception e) {
            log.error("ESPN Crawling failed", e);
            throw new RuntimeException("ESPN 크롤링 중 오류 발생", e);
        }

        return resultList;
    }

    /**
     * 이 전략 구현체가 담당하는 매체 식별자(ESPN)를 반환함
     *
     * @return SourceName.ESPN
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-26
     */
    @Override
    public SourceName getSourceName() {
        return SourceName.ESPN;
    }

    /**
     * API 응답의 개별 기사 JSON 노드를 파싱하여 DTO 객체로 변환함
     * 동영상(Video) 링크일 경우 상세 크롤링을 건너뛰고 요약본(Description)을 본문으로 사용함
     *
     * @param articleNode API 응답 내 개별 기사 정보를 담은 JsonNode
     * @return 파싱된 기사 DTO 객체 (오류 발생 시 null 반환)
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-26
     */
    private CrawledArticleDto parseArticleAndGetDetail(JsonNode articleNode) {
        try {
            String title = articleNode.path("headline").asText();
            String description = articleNode.path("description").asText();
            String link = articleNode.path("links").path("web").path("href").asText();

            String imageUrl = "";
            JsonNode imagesNode = articleNode.path("images");
            if (imagesNode.isArray() && imagesNode.size() > 0) {
                imageUrl = imagesNode.get(0).path("url").asText();
            }
            String author = articleNode.path("byline").asText();


            String fullContent;

            if (link.contains("/video/") || link.contains("watch.espn.com")) {
                // 동영상은 상세 페이지 가봤자 텍스트가 없으므로 API 요약본을 그대로 씀
                log.info("동영상 콘텐츠 감지 (상세 크롤링 스킵): {}", title);
                fullContent = description;
            } else {
                // 일반 기사만 상세 페이지 접속
                fullContent = crawlFullContent(link);
            }

            // 본문 수집 실패 시(혹은 동영상일 때) 요약본으로 대체
            if (fullContent == null || fullContent.isEmpty()) {
                fullContent = description;
            }

            return CrawledArticleDto.builder()
                    .title(title)
                    .content(fullContent)
                    .originalUrl(link)
                    .publisherName("ESPN")
                    .authorName(author.isEmpty() ? "ESPN Staff" : author)
                    .imageUrl(imageUrl)
                    .build();

        } catch (Exception e) {
            log.warn("개별 기사 파싱 중 오류 발생 (URL: {}): {}", articleNode.path("links"), e.getMessage());
            return null;
        }
    }

    /**
     * 상세 기사 URL에 접속하여 HTML 본문을 Jsoup으로 스크래핑함
     * ESPN의 다양한 페이지 구조(React, Game Recap, 구버전 등)에 대응하기 위해 여러 선택자를 순차적으로 시도함
     *
     * @param detailUrl 크롤링할 기사의 상세 페이지 URL
     * @return 추출된 본문 텍스트 (추출 실패 시 null)
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-26
     */
    private String crawlFullContent(String detailUrl) {
        try {
            Document doc = Jsoup.connect(detailUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36") // 봇 탐지 회피용 헤더 강화
                    .timeout(10000)
                    .get();

            // ESPN React 구조
            Elements paragraphs = doc.select("div.Story__Body p");

            // Game Recap 페이지 구조
            if (paragraphs.isEmpty()) {
                paragraphs = doc.select("div.game-recap p");
            }

            // 일반 기사 구조
            if (paragraphs.isEmpty()) {
                paragraphs = doc.select("div.article-body p");
            }

            // 본문처럼 보이는 모든 텍스트 박스 시도
            if (paragraphs.isEmpty()) {
                paragraphs = doc.select("article p, div[class*='story'] p, div[class*='body'] p");
            }

            // 정말 아무것도 못 찾았으면 null 반환 -> 요약본(description)이 대신 들어감
            if (paragraphs.isEmpty()) {
                log.warn("본문 태그를 찾을 수 없음 (구조 변경 가능성): {}", detailUrl);
                return null;
            }

            // 본문이 너무 짧으면 필터링
            String content = paragraphs.text();
            if (content.length() < 50) {
                return null;
            }

            return content;

        } catch (IOException e) {
            log.warn("상세 본문 크롤링 연결 실패: {}", detailUrl);
            return null;
        }
    }
}