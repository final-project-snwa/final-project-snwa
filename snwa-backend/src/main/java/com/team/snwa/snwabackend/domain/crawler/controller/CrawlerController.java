package com.team.snwa.snwabackend.domain.crawler.controller;

import com.team.snwa.snwabackend.domain.crawler.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 크롤링 관련 API 요청을 처리하는 컨트롤러
 * 외부 요청을 받아 특정 Job ID에 해당하는 크롤링 작업을 트리거함
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2026-01-26
 */
@RestController
@RequestMapping("/api/v1/crawler")
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerService crawlerService;

    /**
     * 특정 Job ID를 받아 크롤링 로직을 실행함
     * 작업이 성공적으로 호출되면 성공 메시지를 반환함
     *
     * @param jobId 실행할 크롤링 작업의 식별자 (DB PK)
     * @return 작업 시작 성공 메시지를 담은 ResponseEntity 문자열
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-26
     */
    @PostMapping("/jobs/{jobId}")
    public ResponseEntity<String> runCrawlingJob(@PathVariable Long jobId) {
        crawlerService.executeJob(jobId);
        return ResponseEntity.ok("크롤링 작업(Job ID: " + jobId + ")이 성공적으로 시작되었습니다. 로그를 확인하세요.");
    }
}