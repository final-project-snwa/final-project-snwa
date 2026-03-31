package com.team.snwa.snwabackend.domain.translation.controller;

import com.team.snwa.snwabackend.domain.translation.scheduler.SummaryScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 요약 스케줄러 수동 실행 컨트롤러 (관리자 전용)
 * 요약이 없는 번역본을 찾아 Gemini 요약을 일괄 실행한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SummarySchedulerController {

    private final SummaryScheduler summaryScheduler;

    @PostMapping("/summary")
    public ResponseEntity<Map<String, String>> runSummary() {
        log.info("요약 스케줄러 수동 실행 요청");
        try {
            summaryScheduler.process();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "요약 스케줄러 실행 완료");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("요약 스케줄러 실행 중 오류 발생", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "요약 스케줄러 실행 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}