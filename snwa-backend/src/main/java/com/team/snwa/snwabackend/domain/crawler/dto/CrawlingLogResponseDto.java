package com.team.snwa.snwabackend.domain.crawler.dto;

import com.team.snwa.snwabackend.domain.crawler.entity.enums.CrawlingStatus;
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CrawlingLogResponseDto {
    private Long logId;
    private Long jobId;
    private String jobName;
    private CrawlingStatus status;
    private int collectedCount;
    private String message;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationSeconds;
}
