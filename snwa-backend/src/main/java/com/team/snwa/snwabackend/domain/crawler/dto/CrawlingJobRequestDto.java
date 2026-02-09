package com.team.snwa.snwabackend.domain.crawler.dto;

import com.team.snwa.snwabackend.domain.crawler.entity.enums.EspnLeague;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.SourceName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자가 새로운 크롤링 작업을 등록할 때 사용하는 요청 객체
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2026-02-09
 */
@Getter
@NoArgsConstructor
public class CrawlingJobRequestDto {

    @NotNull(message = "소스(매체)는 필수입니다.")
    private SourceName sourceName;

    @NotBlank(message = "Job 이름은 필수입니다.")
    private String jobName;

    private String targetUrl;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    private String cronExpression;
    private String customTargetUrl;
    private EspnLeague league;
}