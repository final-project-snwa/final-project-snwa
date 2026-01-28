package com.team.snwa.snwabackend.domain.translation.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 번역과 요약을 자동으로 처리하는 스케줄러
 * 크롤링 완료 후 호출되어 번역/요약이 안된 기사를 찾아 처리함
 * 번역과 요약이 모두 완료될 때까지 동기적으로 실행됨
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TranslationSummaryScheduler {

    private final ArticleRepository articleRepository;
    private final TranslationService translationService;
    private final SummaryService summaryService;

    private static final int BATCH_SIZE = 5; // 한 번에 처리할 기사 개수
    private static final long API_DELAY_MS = 4000;

    // 크롤링 완료 후 호출되어 번역과 요약이 안된 기사를 처리
    public void processTranslationAndSummary() {
        log.info("🔄 번역/요약 스케줄러 시작");

        try {
            // 1. 번역이 필요한 기사 처리
            processTranslation();

            try {
                Thread.sleep(1000); // 1초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("번역/요약 스케줄러가 중단되었습니다.");
                return;
            }

            // 2. 요약이 필요한 기사 처리
            processSummary();

            log.info("✅ 번역/요약 스케줄러 종료 - 모든 작업 완료");
        } catch (Exception e) {
            log.error("번역/요약 스케줄러 실행 중 오류 발생", e);
            throw e; // 오류를 상위로 전파하여 크롤링 작업 실패로 처리
        }
    }

    //번역이 안된 기사를 찾아 번역 처리
    private void processTranslation() {
        try {
            Page<Article> page = articleRepository.findArticlesNeedingTranslation(
                    PageRequest.of(0, BATCH_SIZE)
            );
            List<Article> articlesNeedingTranslation = page.getContent();

            log.info("번역이 필요한 기사 조회 완료. 총 {}개, 이번 배치: {}개",
                    page.getTotalElements(), articlesNeedingTranslation.size());

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

                    log.info("API 할당량 초과 방지를 위해 {}초 대기 중...", API_DELAY_MS / 1000);
                    Thread.sleep(API_DELAY_MS);
                } catch (Exception e) {
                    log.error("기사 번역 실패: articleId={}", article.getId(), e);
                    e.printStackTrace();
                }
            }
            
            log.info("번역 처리 완료: {}개 기사 처리됨", articlesNeedingTranslation.size());
        } catch (Exception e) {
            log.error("번역 처리 중 오류 발생", e);
            e.printStackTrace();
            throw e; // 번역 처리 실패 시 상위로 전파
        }
    }

    //요약이 안된 기사를 찾아 요약 처리
    private void processSummary() {
        try {
            log.info("요약이 필요한 기사 조회 시작...");
            Page<Article> page = articleRepository.findArticlesNeedingSummary(
                    PageRequest.of(0, BATCH_SIZE)
            );
            List<Article> articlesNeedingSummary = page.getContent();

            log.info("요약이 필요한 기사 조회 완료. 총 {}개, 이번 배치: {}개",
                    page.getTotalElements(), articlesNeedingSummary.size());

            if (articlesNeedingSummary.isEmpty()) {
                log.info("요약이 필요한 기사가 없습니다.");
                return;
            }

            log.info("요약이 필요한 기사 {}개 발견", articlesNeedingSummary.size());

            for (Article article : articlesNeedingSummary) {
                try {
                    log.info("기사 요약 시작: articleId={}, translatedContent 길이={}",
                            article.getId(),
                            article.getTranslatedContent() != null ? article.getTranslatedContent().length() : 0);
                    summaryService.summarizeArticle(article.getId());
                    log.info("기사 요약 완료: articleId={}", article.getId());

                    log.info("API 할당량 초과 방지를 위해 {}초 대기 중...", API_DELAY_MS / 1000);
                    Thread.sleep(API_DELAY_MS);
                } catch (Exception e) {
                    log.error("기사 요약 실패: articleId={}", article.getId(), e);
                    e.printStackTrace();
                }
            }
            
            log.info("요약 처리 완료: {}개 기사 처리됨", articlesNeedingSummary.size());
        } catch (Exception e) {
            log.error("요약 처리 중 오류 발생", e);
            e.printStackTrace();
            throw e; // 요약 처리 실패 시 상위로 전파
        }
    }
}