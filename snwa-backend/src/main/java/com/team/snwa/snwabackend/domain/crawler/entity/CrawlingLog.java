package com.team.snwa.snwabackend.domain.crawler.entity;

import com.team.snwa.snwabackend.domain.crawler.entity.enums.CrawlingStatus; // Enum 임포트
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "crawling_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrawlingLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private CrawlingJob crawlingJob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrawlingStatus status;

    private int collectedCount;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Builder
    public CrawlingLog(CrawlingJob crawlingJob, CrawlingStatus status, int collectedCount,
                    String errorMessage, LocalDateTime startTime, LocalDateTime endTime) {
        this.crawlingJob = crawlingJob;
        this.status = status;
        this.collectedCount = collectedCount;
        this.errorMessage = errorMessage;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}