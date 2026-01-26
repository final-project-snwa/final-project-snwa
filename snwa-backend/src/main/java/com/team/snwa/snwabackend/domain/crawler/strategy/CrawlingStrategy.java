package com.team.snwa.snwabackend.domain.crawler.strategy;


import com.team.snwa.snwabackend.domain.crawler.dto.CrawledArticleDto;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.SourceName;

import java.util.List;

/**
 * 모든 크롤러가 따라야 할 공통적인 규칙
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2025-01-26
 */
public interface CrawlingStrategy {


    /**
     * 실제 크롤링 로직을 수행하는 메서드
     *
     * @param url 크롤링할 타겟 URL
     * @return 수집된 기사 목록
     *
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2025-01-26
     */
    List<CrawledArticleDto> crawl(String url);


    /**
     * 해당 전략이 어떤 언론사를 담당하는지 반환하는 메서드
     *
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2025-01-26
     */
    SourceName getSourceName();

}
