package com.team.snwa.snwabackend.domain.translation.controller;

import com.team.snwa.snwabackend.domain.translation.service.TranslationSummaryScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Postman 테스트용 컨트롤
 * 번역/요약 스케줄러를 수동으로 실행할 수 있는 테스트용 컨트롤러
 * 포스트맨에서 테스트하기 위해 사용
*/
@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class TranslationSchedulerController {

    private final TranslationSummaryScheduler translationSummaryScheduler;

    @PostMapping("/process-translation-summary")
    public ResponseEntity<Map<String, String>> processTranslationAndSummary() {
        log.info("📞 스케줄러 수동 실행 요청 받음");

        try {
            translationSummaryScheduler.processTranslationAndSummary();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "번역/요약 스케줄러 실행 완료");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("스케줄러 실행 중 오류 발생", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "스케줄러 실행 실패: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
}