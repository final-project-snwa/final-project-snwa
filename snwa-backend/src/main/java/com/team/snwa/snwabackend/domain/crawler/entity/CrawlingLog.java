package com.team.snwa.snwabackend.domain.crawler.entity;

import com.team.snwa.snwabackend.domain.crawler.entity.enums.CrawlingStatus; // Enum 임포트
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 개별 크롤링 작업의 실행 이력(로그)을 저장하는 엔티티
 * 작업의 성공/실패 여부(Status), 수집된 데이터 개수, 에러 메시지 및 실행 시간을 기록하여 모니터링에 활용됨
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2026-01-26
 */
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
    private String message; // 에러메세지

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

    /**
     * 크롤링 작업이 성공적으로 완료되었을 때 상태를 업데이트함
     * 성공 상태로 변경하고, 수집된 기사 개수와 종료 시간을 기록함
     *
     * @param count 수집에 성공하여 DB에 저장된 기사의 개수
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-26
     */
    public void updateSuccess(int count) {
        this.status = CrawlingStatus.SUCCESS;
        this.collectedCount = count;
        this.endTime = LocalDateTime.now(); // 끝난 시간 기록
    }

    /**
     * 크롤링 작업 중 예외가 발생하여 실패했을 때 상태를 업데이트함
     * 실패 상태로 변경하고, 발생한 에러 메시지와 종료 시간을 기록함
     *
     * @param errorMessage 발생한 예외(Exception)의 메시지 내용
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-26
     */
    public void updateFailure(String errorMessage) {
        this.status = CrawlingStatus.FAILURE;
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now(); // 실패하더라도 끝난 시간은 기록
    }
}