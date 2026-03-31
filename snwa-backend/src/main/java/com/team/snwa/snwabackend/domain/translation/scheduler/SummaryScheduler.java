package com.team.snwa.snwabackend.domain.translation.scheduler;

import com.team.snwa.snwabackend.domain.translation.entity.ArticleTranslation;
import com.team.snwa.snwabackend.domain.translation.repository.ArticleTranslationRepository;
import com.team.snwa.snwabackend.domain.translation.service.KeywordExtractionService;
import com.team.snwa.snwabackend.domain.translation.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 요약을 자동으로 처리하는 스케줄러
 * 번역 완료 후 호출되어 요약이 안된 기사를 찾아 처리함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryScheduler {

    private final ArticleTranslationRepository articleTranslationRepository;
    private final SummaryService summaryService;
    private final KeywordExtractionService keywordExtractionService;

    private static final int BATCH_SIZE = 2; // 한 번에 처리할 기사 개수
    private static final long API_DELAY_MS = 4000;

    /**
     * 요약이 필요한 기사를 찾아 요약 처리
     */
    public void process() {
        log.info("🔄 요약 스케줄러 시작");

        try {
            Page<ArticleTranslation> page = articleTranslationRepository.findTranslationsNeedingSummary("KO",
                    PageRequest.of(0, BATCH_SIZE));
            List<ArticleTranslation> translations = page.getContent();

            if (translations.isEmpty()) {
                log.info("요약 대상 기사 없음");
                return;
            }

            log.info("요약 대상 기사: 전체 {}개, 이번 실행 {}개",
                    page.getTotalElements(), translations.size());

            int failCount = 0;
            List<Long> successIds = new ArrayList<>();

            for (ArticleTranslation translation : translations) {
                try {
                    log.debug("기사 요약 시작: articleId={}, contentLength={}",
                            translation.getArticle().getId(),
                            translation.getTranslatedContent() != null
                                    ? translation.getTranslatedContent().length()
                                    : 0);
                    summaryService.summarizeArticle(translation.getArticle().getId());
                    successIds.add(translation.getArticle().getId());
                    Thread.sleep(API_DELAY_MS);
                } catch (Exception e) {
                    failCount++;
                    log.error("기사 요약 실패: articleId={}", translation.getArticle().getId(), e);
                }
            }

            log.info("✅ 요약 스케줄러 완료: articleId {} 요약 완료 (성공 {}, 실패 {})",
                    successIds, successIds.size(), failCount);
        } catch (Exception e) {
            log.error("요약 스케줄러 실행 중 오류 발생", e);
            throw e;
        }
    }
}