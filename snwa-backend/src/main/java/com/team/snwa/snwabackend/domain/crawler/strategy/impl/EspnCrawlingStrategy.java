package com.team.snwa.snwabackend.domain.crawler.strategy.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.snwa.snwabackend.domain.crawler.dto.CrawledArticleDto;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.SourceName;
import com.team.snwa.snwabackend.domain.crawler.strategy.CrawlingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EspnCrawlingStrategy implements CrawlingStrategy {

    private final ObjectMapper objectMapper;

    private static final String ESPN_NBA_API_URL = "http://site.api.espn.com/apis/site/v2/sports/basketball/nba/news";

    @Override
    public List<CrawledArticleDto> crawl(String url) {
        return List.of();
    }

    @Override
    public SourceName getSourceName() {
        return null;
    }
}
