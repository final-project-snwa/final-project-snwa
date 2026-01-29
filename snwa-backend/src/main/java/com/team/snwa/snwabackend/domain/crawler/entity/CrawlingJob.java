package com.team.snwa.snwabackend.domain.crawler.entity;

import com.team.snwa.snwabackend.domain.article.entity.Category;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.SourceName;
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 크롤링 수행 작업을 정의하는 설정 정보 엔티티
 * 수집 대상(Source, URL), 수집 주기(Cron), 활성화 여부 등을 관리하며 스케줄러가 참조하는 기준 데이터임
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2026-01-26
 */
@Entity
@Table(name = "crawling_job")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlingJob extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // 종목 분류

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private SourceName sourceName; // ESPN, BBC 등

    private String jobName;

    private String targetUrl; // 타겟 URL

    private String cronExpression; // 수집 주기

    @Column(columnDefinition = "TINYINT(1)")
    private boolean isActive; // 활성 여부

    private LocalDateTime lastRunAt; // 마지막 실행일

    public void updateLastRunAt() {
        this.lastRunAt = LocalDateTime.now();
    }

    /**
     * 크롤링 실행 주기를 변경함 (Dirty Checking용)
     *
     * @param cronExpression 변경할 Cron 표현식
     * @author 허준형
     * @DateOfCreated 2026-01-27
     */
    public void updateCron(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    /**
     * 작업의 활성/비활성 상태를 변경함 (Dirty Checking용)
     *
     * @param isActive true: 활성, false: 중지
     * @author 허준형
     * @DateOfCreated 2026-01-27
     */
    public void updateActive(boolean isActive) {
        this.isActive = isActive;
    }
}