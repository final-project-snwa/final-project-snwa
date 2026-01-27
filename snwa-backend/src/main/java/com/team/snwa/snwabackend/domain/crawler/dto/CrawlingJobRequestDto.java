package com.team.snwa.snwabackend.domain.crawler.dto;

import com.team.snwa.snwabackend.domain.crawler.entity.enums.EspnLeague;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.SourceName;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자가 새로운 크롤링 작업을 등록할 때 사용하는 요청 객체
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2026-01-26
 */
@Getter
@NoArgsConstructor
public class CrawlingJobRequestDto {
    private SourceName sourceName;
    private String jobName;
    private String targetUrl;
    private Long categoryId;
    private String cronExpression;
    private String customTargetUrl;
    private EspnLeague league;
}