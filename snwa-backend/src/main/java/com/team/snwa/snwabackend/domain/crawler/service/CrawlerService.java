package com.team.snwa.snwabackend.domain.crawler.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.crawler.dto.CrawledArticleDto;
import com.team.snwa.snwabackend.domain.crawler.entity.ArticleCrawlingTracking;
import com.team.snwa.snwabackend.domain.crawler.entity.CrawlingJob;
import com.team.snwa.snwabackend.domain.crawler.entity.CrawlingLog;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.CrawlingStatus;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.SourceName;
import com.team.snwa.snwabackend.domain.crawler.repository.ArticleCrawlingTrackingRepository;
import com.team.snwa.snwabackend.domain.crawler.repository.CrawlingJobRepository;
import com.team.snwa.snwabackend.domain.crawler.repository.CrawlingLogRepository;
import com.team.snwa.snwabackend.domain.crawler.strategy.CrawlingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 크롤링 작업의 전반적인 라이프사이클을 관리하는 비즈니스 로직 클래스
 * Job 조회, 적절한 전략 탐색, 데이터 수집 및 결과 저장(Article, Log)을 수행함
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2026-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerService {

    private final List<CrawlingStrategy> crawlingStrategies;
    private final CrawlingJobRepository jobRepository;
    private final CrawlingLogRepository logRepository;
    private final ArticleRepository articleRepository;
    private final ArticleCrawlingTrackingRepository trackingRepository;

    /**
     * 특정 Job ID를 받아 크롤링 프로세스를 실행함
     * 로그 기록(시작/성공/실패)을 관리하며, 수집된 데이터를 Article 및 Tracking 엔티티로 변환하여 저장함
     *
     * @param jobId 실행할 크롤링 작업의 고유 식별자
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-26
     */
    @Transactional
    public void executeJob(Long jobId) {
        // Job 조회
        CrawlingJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Job ID입니다: " + jobId));

        // 로그 생성 (시작)
        CrawlingLog crawlingLog = CrawlingLog.builder()
                .crawlingJob(job)
                .status(CrawlingStatus.RUNNING)
                .startTime(LocalDateTime.now())
                .collectedCount(0)
                .build();
        logRepository.save(crawlingLog);

        try {
            // 전략 찾기
            CrawlingStrategy strategy = findStrategy(job.getSourceName());

            // 크롤링 수행
            List<CrawledArticleDto> articles = strategy.crawl(job.getTargetUrl());

            int savedCount = 0;

            // 결과 저장
            for (CrawledArticleDto dto : articles) {
                if (saveArticleIfNotExists(dto, job)) {
                    savedCount++;
                }
            }

            // 성공 상태 업데이트
            crawlingLog.updateSuccess(savedCount);

        } catch (Exception e) {
            log.error("크롤링 작업 실패: JobId=" + jobId, e);
            // 실패 상태 업데이트
            crawlingLog.updateFailure(e.getMessage());
        }
    }

    /**
     * Job에 설정된 SourceName에 대응하는 크롤링 전략 구현체를 찾아서 반환함
     *
     * @param sourceName ESPN, BBC 등 매체 식별 Enum
     * @return 해당 매체의 크롤링 로직을 담은 CrawlingStrategy 구현체
     * @throws IllegalArgumentException 지원하지 않는 소스일 경우 발생
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-26
     */
    private CrawlingStrategy findStrategy(SourceName sourceName) {
        return crawlingStrategies.stream()
                .filter(s -> s.getSourceName() == sourceName)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 소스입니다: " + sourceName));
    }


    /**
     * 수집된 기사 DTO를 검증하고 중복이 없을 경우 DB에 저장함
     * Article 엔티티와 추적용 Tracking 엔티티를 함께 생성하여 저장함
     *
     * @param dto 크롤링 전략을 통해 수집된 기사 데이터 객체
     * @param job 현재 실행 중인 크롤링 작업 정보
     * @return 저장 성공 여부 (true: 저장됨, false: 중복 또는 에러로 스킵됨)
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-26
     */
    private boolean saveArticleIfNotExists(CrawledArticleDto dto, CrawlingJob job) {
        // 중복 검사
        if (articleRepository.existsByOriginalUrl(dto.getOriginalUrl())) {
            log.info("중복된 기사 스킵: {}", dto.getTitle());
            return false;
        }

        // 안전장치
        if (job.getCategory() == null) {
            log.error("Job(ID: {})에 연결된 카테고리 정보가 없습니다.", job.getId());
            return false;
        }

        Article article = Article.builder()
                .category(job.getCategory())
                .title(dto.getTitle())
                .content(dto.getContent())
                .originalUrl(dto.getOriginalUrl())
                .authorName(dto.getAuthorName())
                .publisherName(dto.getPublisherName())
                .imageUrl(dto.getImageUrl())
                .translatedContent(null)
                .summary(null)
                .build();

        Article savedArticle = articleRepository.save(article);

        // Tracking 정보 저장
        ArticleCrawlingTracking tracking = new ArticleCrawlingTracking();
        tracking.setArticle(savedArticle);
        tracking.setJobId(job.getId());
        tracking.setCategoryId(job.getCategory().getId());
        tracking.setArticleUrl(dto.getOriginalUrl());
        tracking.setTitleOrigin(dto.getTitle());
        tracking.setContentOrigin(dto.getContent());
        trackingRepository.save(tracking);

        return true;
    }
}