package com.team.snwa.snwabackend.domain.translation.controller;

import com.team.snwa.snwabackend.domain.translation.scheduler.KeywordsTagScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 태그 추출 스케줄러 수동 실행 컨트롤러 (관리자 전용)
 * 태그가 없는 번역본을 찾아 Gemini 키워드 추출을 일괄 실행한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class KeywordsTagSchedulerController {

    private final KeywordsTagScheduler keywordsTagScheduler;

    @PostMapping("/keywords")
    public ResponseEntity<Map<String, String>> runKeywords() {
        log.info("태그 추출 스케줄러 수동 실행 요청");
        try {
            keywordsTagScheduler.process();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "태그 추출 스케줄러 실행 완료");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("태그 추출 스케줄러 실행 중 오류 발생", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "태그 추출 스케줄러 실행 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}