package com.team.snwa.snwabackend.domain.translation.scheduler;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.translation.service.KeywordExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 키워드 추출(태그)을 자동으로 처리하는 스케줄러
 * 번역 완료 후 호출되어 태그가 없는 기사를 찾아 키워드를 추출하여 태그로 저장함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordsTagScheduler {

    private final ArticleRepository articleRepository;
    private final KeywordExtractionService keywordExtractionService;

    private static final int BATCH_SIZE = 2; // 한 번에 처리할 기사 개수
    private static final long API_DELAY_MS = 4000;

    /**
     * 키워드 추출이 필요한 기사를 찾아 태그 추출 처리
     */
    public void process() {
        log.info("🔄 키워드 태그 추출 스케줄러 시작");

        try {
            Page<Article> page = articleRepository.findArticlesNeedingKeywordExtraction(
                    PageRequest.of(0, BATCH_SIZE)
            );
            List<Article> articles = page.getContent();

            if (articles.isEmpty()) {
                log.info("키워드 추출할 기사 없음");
                return;
            }

            log.info("키워드 추출 대상: 전체 {}개, 이번 실행 {}개",
                    page.getTotalElements(), articles.size());

            int failCount = 0;

            for (Article article : articles) {
                try {
                    log.debug("기사 키워드 추출 시작: articleId={}", article.getId());
                    keywordExtractionService.extractKeywords(article.getId(), "KO");
                    Thread.sleep(API_DELAY_MS);
                } catch (Exception e) {
                    failCount++;
                    log.error("기사 키워드 추출 실패: articleId={}", article.getId(), e);
                }
            }

            log.info("✅ 키워드 태그 추출 스케줄러 완료: 성공 {}, 실패 {}",
                    articles.size() - failCount, failCount);
        } catch (Exception e) {
            log.error("키워드 태그 추출 스케줄러 실행 중 오류 발생", e);
            throw e;
        }
    }
}