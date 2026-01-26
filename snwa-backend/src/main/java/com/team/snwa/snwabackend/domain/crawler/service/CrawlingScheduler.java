package com.team.snwa.snwabackend.domain.crawler.service;

import com.team.snwa.snwabackend.domain.crawler.entity.CrawlingJob;
import com.team.snwa.snwabackend.domain.crawler.repository.CrawlingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 정기적인 크롤링 작업을 트리거하는 스케줄러 클래스
 * Spring Scheduler를 사용하여 설정된 주기(Cron)마다 활성화된 Job을 조회하고 실행을 위임함
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2026-01-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlingScheduler {

    private final CrawlingJobRepository crawlingJobRepository;
    private final CrawlerService crawlerService;

    /**
     * 주기적으로 실행되어 실제 크롤링 로직을 호출하는 메서드
     * DB에서 활성화된 상태의 모든 Job을 조회하여 순차적으로 실행함
     * 현재 테스트 목적으로 10초마다 실행되도록 설정됨
     * TODO : 운영 배포 시 Cron 표현식 수정 필요
     *
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-26
     */
    // @Scheduled(cron = "0 0 * * * *") // 운영용: 매 정각 실행
    @Scheduled(cron = "0/10 * * * * *") // 테스트용: 10초마다 실행
    public void scheduleCrawling() {
        log.info("⏰ 정기 크롤링 스케줄러 시작");

        // 활성화된 Job만 가져오기
        List<CrawlingJob> activeJobs = crawlingJobRepository.findByIsActiveTrue();

        if (activeJobs.isEmpty()) {
            log.info("할 일이 없습니다. (활성화된 Job 없음)");
            return;
        }

        //  각 Job 실행
        for (CrawlingJob job : activeJobs) {
            log.info("Job 실행 중: {} (ID: {})", job.getSourceName(), job.getId());
            crawlerService.executeJob(job.getId());
        }

        log.info("✅ 정기 크롤링 스케줄러 종료");
    }
}