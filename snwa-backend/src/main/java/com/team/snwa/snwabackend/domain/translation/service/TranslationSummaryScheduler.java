package com.team.snwa.snwabackend.domain.translation.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 번역과 요약을 자동으로 처리하는 스케줄러
 * 30분마다 실행되어 번역/요약이 안된 기사를 찾아 처리함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TranslationSummaryScheduler {

    private final ArticleRepository articleRepository;
    private final TranslationService translationService;
    private final SummaryService summaryService;

    private static final int BATCH_SIZE = 10; // 한 번에 처리할 기사 개수

    // 30분마다 실행되어 번역과 요약이 안된 기사를 처리
    @Scheduled(cron = "0 */30 * * * *")
    public void processTranslationAndSummary() {
        log.info("🔄 번역/요약 스케줄러 시작");

        // 1. 번역이 필요한 기사 처리
        processTranslation();

        // 2. 요약이 필요한 기사 처리
        processSummary();

        log.info("✅ 번역/요약 스케줄러 종료");
    }

    //번역이 안된 기사를 찾아 번역 처리
    private void processTranslation() {
        try {
            Page<Article> page = articleRepository.findArticlesNeedingTranslation(
                    PageRequest.of(0, BATCH_SIZE)
            );
            List<Article> articlesNeedingTranslation = page.getContent();

            if (articlesNeedingTranslation.isEmpty()) {
                log.info("번역이 필요한 기사가 없습니다.");
                return;
            }

            log.info("번역이 필요한 기사 {}개 발견", articlesNeedingTranslation.size());

            for (Article article : articlesNeedingTranslation) {
                try {
                    log.info("기사 번역 시작: articleId={}, title={}", article.getId(), article.getTitle());
                    translationService.translateArticle(article.getId());
                    log.info("기사 번역 완료: articleId={}", article.getId());
                } catch (Exception e) {
                    log.error("기사 번역 실패: articleId={}", article.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("번역 처리 중 오류 발생", e);
        }
    }

    //약이 안된 기사를 찾아 요약 처리
    private void processSummary() {
        try {
            Page<Article> page = articleRepository.findArticlesNeedingSummary(
                    PageRequest.of(0, BATCH_SIZE)
            );
            List<Article> articlesNeedingSummary = page.getContent();

            if (articlesNeedingSummary.isEmpty()) {
                log.info("요약이 필요한 기사가 없습니다.");
                return;
            }

            log.info("요약이 필요한 기사 {}개 발견", articlesNeedingSummary.size());

            for (Article article : articlesNeedingSummary) {
                try {
                    log.info("기사 요약 시작: articleId={}", article.getId());
                    summaryService.summarizeArticle(article.getId());
                    log.info("기사 요약 완료: articleId={}", article.getId());
                } catch (Exception e) {
                    log.error("기사 요약 실패: articleId={}", article.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("요약 처리 중 오류 발생", e);
        }
    }
}