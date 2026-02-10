package com.team.snwa.snwabackend.domain.crawler.controller;

import com.team.snwa.snwabackend.domain.crawler.dto.CrawlingJobRequestDto;
import com.team.snwa.snwabackend.domain.crawler.dto.CrawlingJobResponseDto;
import com.team.snwa.snwabackend.domain.crawler.dto.CrawlingJobUpdateDto;
import com.team.snwa.snwabackend.domain.crawler.dto.CrawlingLogResponseDto;
import com.team.snwa.snwabackend.domain.crawler.dto.SystemStatusDto;
import com.team.snwa.snwabackend.domain.crawler.entity.CrawlingJob;
import com.team.snwa.snwabackend.domain.crawler.service.CrawlerService;
import com.team.snwa.snwabackend.domain.crawler.service.DynamicSchedulingService;
import com.team.snwa.snwabackend.domain.crawler.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 크롤링 작업(Job)을 관리하는 관리자 전용 컨트롤러
 * 조회, 생성, 수정(중단/재개/스케줄링), 삭제, 즉시 실행 기능을 제공함
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2026-01-27
 */
@RestController
@RequestMapping("/api/admin/crawler")
@RequiredArgsConstructor
public class AdminCrawlerController {

    private final CrawlerService crawlerService;
    private final DynamicSchedulingService schedulingService;
    private final SystemMonitorService systemMonitorService;

    /**
     * 목록 조회 - 모든 크롤링 Job 리스트 반환
     *
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-27
     */
    @GetMapping("/jobs")
    public ResponseEntity<List<CrawlingJobResponseDto>> getAllJobs() {
        return ResponseEntity.ok(crawlerService.getAllJobs());
    }



    /**
     * 새로운 Job 생성 및 스케줄러 등록
     *
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-02-09
     */
    @PostMapping("/jobs")
    public ResponseEntity<String> createJob(@Valid @RequestBody CrawlingJobRequestDto request) {
        Long newJobId = crawlerService.createCrawlingJob(request);
        schedulingService.startJob(newJobId);
        return ResponseEntity.ok("새로운 크롤링 Job이 등록되고 스케줄러가 시작되었습니다. (ID: " + newJobId + ")");
    }

    /**
     * 크롤링 중단, 재개, 스케줄 변경
     *
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-27
     */
    @PatchMapping("/jobs/{jobId}")
    public ResponseEntity<String> updateJob(@PathVariable Long jobId, @RequestBody CrawlingJobUpdateDto dto) {
        CrawlingJob updatedJob = crawlerService.updateCrawlingJob(jobId, dto);

        if (Boolean.TRUE.equals(updatedJob.isActive())) {
            schedulingService.startJob(jobId);
            return ResponseEntity.ok("Job(ID: " + jobId + ")이 수정되었으며 스케줄러가 (재)시작되었습니다.");
        } else {
            schedulingService.stopJob(jobId);
            return ResponseEntity.ok("Job(ID: " + jobId + ")이 수정되었으며 스케줄링이 중단되었습니다.");
        }
    }

    /**
     * Job 삭제 및 스케줄러 제거
     *
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-27
     */
    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<String> deleteJob(@PathVariable Long jobId) {
        schedulingService.stopJob(jobId);
        crawlerService.deleteCrawlingJob(jobId);
        return ResponseEntity.ok("Job(ID: " + jobId + ")이 성공적으로 삭제되었습니다.");
    }

    /**
     * 관리자 수동 트리거
     *
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-27
     */
    @PostMapping("/jobs/{jobId}/run")
    public ResponseEntity<String> runJobManually(@PathVariable Long jobId) {
        crawlerService.executeJob(jobId);
        return ResponseEntity.ok("관리자 요청으로 Job ID " + jobId + " 가 즉시 실행되었습니다.");
    }

    /**
     * 전체 중지
     *
     * @author 허준형
     * @DateOfCreated 2026-01-28
     * @DateOfEdit 2026-01-28
     */
    @PostMapping("/global/stop")
    public ResponseEntity<String> stopAllJobs() {
        schedulingService.stopAll();
        return ResponseEntity.ok("🚨 모든 크롤링 스케줄이 중단되었습니다. (Emergency Stop)");
    }

    /**
     * 전체 시작
     *
     * @author 허준형
     * @DateOfCreated 2026-01-28
     * @DateOfEdit 2026-01-28
     */
    @PostMapping("/global/start")
    public ResponseEntity<String> startAllJobs() {
        schedulingService.startAll();
        return ResponseEntity.ok("🔄 모든 활성 작업이 스케줄러에 재등록되었습니다.");
    }

    /**
     * 현재 실제로 돌고 있는 스케줄 ID 목록 조회
     *
     * @author 허준형
     * @DateOfCreated 2026-01-28
     * @DateOfEdit 2026-01-28
     */
    @GetMapping("/global/status")
    public ResponseEntity<List<Long>> getRunningStatus() {
        List<Long> runningIds = schedulingService.getRunningJobIds();
        return ResponseEntity.ok(runningIds);
    }

    /**
     * 서버 상태 확인 - CPU 코어, 메모리 사용량, 활성 스레드, 크롤러 가동 현황 조회
     *
     * @author 허준형
     * @DateOfCreated 2026-01-28
     * @DateOfEdit 2026-01-28
     */
    @GetMapping("/status")
    public ResponseEntity<SystemStatusDto> getSystemStatus() {
        // 컨트롤러는 "가져와!" 명령만 하고, 결과만 바로 반환 (Toss)
        return ResponseEntity.ok(systemMonitorService.getCurrentSystemStatus());
    }

    /**
     * 롤링 수행 이력을 페이징하여 조회함
     * jobId 파라미터를 넣으면 해당 작업의 이력만 필터링 가능
     *
     * @author 허준형
     * @DateOfCreated 2026-01-28
     * @DateOfEdit 2026-01-28
     */
    @GetMapping("/logs")
    public ResponseEntity<Page<CrawlingLogResponseDto>> getCrawlingLogs(
            @RequestParam(required = false) Long jobId,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // PageableDefault: 기본적으로 최신순(DESC) 20개씩 가져옴
        return ResponseEntity.ok(crawlerService.getCrawlingLogs(jobId, pageable));
    }
}