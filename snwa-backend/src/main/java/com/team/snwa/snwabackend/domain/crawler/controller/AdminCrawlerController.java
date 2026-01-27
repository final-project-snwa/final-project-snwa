package com.team.snwa.snwabackend.domain.crawler.controller;

import com.team.snwa.snwabackend.domain.crawler.dto.CrawlingJobRequestDto;
import com.team.snwa.snwabackend.domain.crawler.dto.CrawlingJobRequestDto;
import com.team.snwa.snwabackend.domain.crawler.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 크롤링 작업(Job)을 관리하는 관리자 전용 컨트롤러
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2026-01-26
 */
@RestController
@RequestMapping("/api/admin/crawler")
@RequiredArgsConstructor
public class AdminCrawlerController {

    private final CrawlerService crawlerService;

    /**
     * 새로운 크롤링 Job을 생성함
     *
     * @param request 생성할 Job의 정보(소스, URL, 카테고리ID 등)
     * @return 성공 메시지
     */
    @PostMapping("/jobs")
    public ResponseEntity<String> createJob(@RequestBody CrawlingJobRequestDto request) {
        crawlerService.createCrawlingJob(request);
        return ResponseEntity.ok("새로운 크롤링 Job이 등록되었습니다.");
    }
}