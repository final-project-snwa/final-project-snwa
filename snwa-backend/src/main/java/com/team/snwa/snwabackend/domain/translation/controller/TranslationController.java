package com.team.snwa.snwabackend.domain.translation.controller;

import com.team.snwa.snwabackend.domain.translation.dto.response.SummaryResponseDto;
import com.team.snwa.snwabackend.domain.translation.dto.response.TranslatedArticleResponseDto;
import com.team.snwa.snwabackend.domain.translation.service.ArticleOrchestratorService;
import com.team.snwa.snwabackend.domain.translation.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/article/{articleId}")
@RequiredArgsConstructor
public class TranslationController {

    private final ArticleOrchestratorService articleOrchestratorService;
    private final SummaryService summaryService;

    @PostMapping("/translation/test")
    public ResponseEntity<TranslatedArticleResponseDto> translateArticle(
            @PathVariable Long articleId) {
        log.info("번역 요청 받음: articleId={}", articleId);

        TranslatedArticleResponseDto response = articleOrchestratorService.translateArticle(articleId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/summary")
    public ResponseEntity<SummaryResponseDto> summarizeArticle(@PathVariable Long articleId) {
        log.info("요약 요청 받음: articleId={}", articleId);
        SummaryResponseDto response = summaryService.summarizeArticle(articleId);
        return ResponseEntity.ok(response);
    }
}

// Postman 테스트용 컨트롤