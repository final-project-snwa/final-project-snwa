package com.team.snwa.snwabackend.domain.crawler.service;

import com.team.snwa.snwabackend.domain.crawler.entity.CrawlingJob;
import com.team.snwa.snwabackend.domain.crawler.repository.CrawlingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;


/**
 * DB에 저장된 Cron 표현식에 따라 동적으로 스케줄을 관리하는 서비스
 * 기존 CrawlingScheduler를 대체함.
 *
 * @author 허준형
 * @DateOfCreated 2026-01-27
 * @DateOfEdit 2026-01-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicSchedulingService {

    private final TaskScheduler taskScheduler;
    private final CrawlingJobRepository crawlingJobRepository;
    private final CrawlerService crawlerService;

    /**
     * 실행된 작업의 핸들(ScheduledFuture)을 Map에 저장해두었다가
     * 필요할 때 꺼내서 정지하거나 갈아끼움
     *
     *
     * @author 허준형
     * @DateOfCreated 2026-01-27
     * @DateOfEdit 2026-01-27
     */
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();


    /**
     * 서버가 시작되자마자 DB에 있는 활성 Job들을 전부 스케줄러에 등록하는 메서드
     * (기존 Scheduler의 @Scheduled 역할을 대신함)
     * @author 허준형
     * @DateOfCreated 2026-01-27
     * @DateOfEdit 2026-01-27
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initSchedules() {
        log.info("🚀 서버 시작: 저장된 크롤링 작업을 스케줄러에 등록합니다...");
        List<CrawlingJob> activeJobs = crawlingJobRepository.findByIsActiveTrue(); // 메서드명 수정됨

        for (CrawlingJob job : activeJobs) {
            startJob(job.getId());
        }
    }

    /**
     * 특정 Job을 스케줄러에 등록/재등록
     */
    public void startJob(Long jobId) {

        stopJob(jobId);

        CrawlingJob job = crawlingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        if (!job.isActive()) {
            log.info("Job ID {} is inactive. Skipping.", jobId);
            return;
        }

        Runnable task = () -> {
            log.info("⏰ 스케줄러 작동! Job ID: {} 실행 시도...", jobId);
            try {
                crawlerService.executeJob(job.getId());
            } catch (Exception e) {
                log.error("크롤링 실행 중 에러 발생 - JobId: {}", jobId, e);
            }
        };

        // 스케줄 등록
        try {
            CronTrigger trigger = new CronTrigger(job.getCronExpression());
            ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);

            // 리모컨 저장
            scheduledTasks.put(jobId, future);
            log.info("✅ 스케줄 등록 완료: {} (Cron: {})", job.getJobName(), job.getCronExpression());
        } catch (IllegalArgumentException e) {
            log.error("❌ 잘못된 Cron 표현식입니다. JobId: {}, Expr: {}", jobId, job.getCronExpression());
        }
    }

    /**
     * 특정 Job 스케줄 중지
     */
    public void stopJob(Long jobId) {
        ScheduledFuture<?> future = scheduledTasks.get(jobId);
        if (future != null) {
            future.cancel(false);
            scheduledTasks.remove(jobId);
            log.info("⏹ 스케줄 중지 완료: JobId {}", jobId);
        }
    }

    public void stopAll() {
        // Map을 돌면서 하나씩 끄면 충돌 날 수 있으니, 현재 켜진 ID 목록을 먼저 복사함
        List<Long> runningJobIds = new ArrayList<>(scheduledTasks.keySet());

        for (Long jobId : runningJobIds) {
            stopJob(jobId);
        }
        log.warn("🚨 긴급 정지: 모든 크롤링 스케줄러가 중단되었습니다.");
    }

    public void startAll() {
        log.info("🔄 전체 재시작: 활성화된 모든 작업을 스케줄러에 등록합니다.");
        // 기존 initSchedules 메서드 로직 재사용
        initSchedules();
    }

    public List<Long> getRunningJobIds() {
        return new ArrayList<>(scheduledTasks.keySet());
    }
}
