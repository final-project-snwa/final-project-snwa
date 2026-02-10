package com.team.snwa.snwabackend.domain.crawler.dto;

import com.team.snwa.snwabackend.domain.article.entity.enums.CategoryName;
import com.team.snwa.snwabackend.domain.crawler.entity.CrawlingJob;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.SourceName;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CrawlingJobResponseDto {
    private Long id;
    private Long categoryId;
    private CategoryName categoryName;
    private SourceName sourceName;
    private String jobName;
    private String targetUrl;
    private String cronExpression;
    private boolean isActive;
    private LocalDateTime lastRunAt;

    public static CrawlingJobResponseDto from(CrawlingJob job) {
        return CrawlingJobResponseDto.builder()
                .id(job.getId())
                .categoryId(job.getCategory().getId())
                .categoryName(job.getCategory().getCategoryName())
                .sourceName(job.getSourceName())
                .jobName(job.getJobName())
                .targetUrl(job.getTargetUrl())
                .cronExpression(job.getCronExpression())
                .isActive(job.isActive())
                .lastRunAt(job.getLastRunAt())
                .build();
    }
}
