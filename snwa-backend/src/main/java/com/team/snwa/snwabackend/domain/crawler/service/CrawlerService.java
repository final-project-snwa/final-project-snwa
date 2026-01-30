package com.team.snwa.snwabackend.domain.crawler.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.entity.Category;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.article.repository.CategoryRepository;
import com.team.snwa.snwabackend.domain.crawler.dto.CrawledArticleDto;
import com.team.snwa.snwabackend.domain.crawler.dto.CrawlingJobRequestDto;
import com.team.snwa.snwabackend.domain.crawler.dto.CrawlingJobUpdateDto;
import com.team.snwa.snwabackend.domain.crawler.dto.CrawlingLogResponseDto;
import com.team.snwa.snwabackend.domain.crawler.entity.ArticleCrawlingTracking;
import com.team.snwa.snwabackend.domain.crawler.entity.CrawlingJob;
import com.team.snwa.snwabackend.domain.crawler.entity.CrawlingLog;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.CrawlingStatus;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.EspnLeague;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.SourceName;
import com.team.snwa.snwabackend.domain.crawler.repository.ArticleCrawlingTrackingRepository;
import com.team.snwa.snwabackend.domain.crawler.repository.CrawlingJobRepository;
import com.team.snwa.snwabackend.domain.crawler.repository.CrawlingLogRepository;
import com.team.snwa.snwabackend.domain.crawler.strategy.CrawlingStrategy;
import com.team.snwa.snwabackend.domain.translation.scheduler.KeywordsTagScheduler;
import com.team.snwa.snwabackend.domain.translation.scheduler.SummaryScheduler;
import com.team.snwa.snwabackend.domain.translation.scheduler.TranslationScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    private final CategoryRepository categoryRepository;
    private final TranslationScheduler translationScheduler;
    private final SummaryScheduler summaryScheduler;
    private final KeywordsTagScheduler keywordsTagScheduler;

    private static final String ESPN_BASE_URL = "http://site.api.espn.com/apis/site/v2/sports/";

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

            // 크롤링 완료 후 번역/요약 스케줄러 실행 (트랜잭션 커밋 후 실행)
            if (savedCount > 0) {
                log.info("크롤링 완료: {}개 기사 저장됨", savedCount);

                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronizationAdapter() {
                            @Override
                            public void afterCommit() {
                                try {
                                    // 1. 번역 처리
                                    log.info("번역 스케줄러 실행");
                                    translationScheduler.process();

                                    Thread.sleep(1000); // 1초 대기

                                    // 2. 요약 처리
                                    log.info("요약 스케줄러 실행");
                                    summaryScheduler.process();

                                    Thread.sleep(1000); // 1초 대기

                                    // 3. 키워드 태그 추출 처리
                                    log.info("키워드 태그 추출 스케줄러 실행");
                                    keywordsTagScheduler.process();

                                    log.info("✅ 모든 후속 작업 완료");
                                } catch (Exception e) {
                                    log.error("후속 작업 중 오류 발생", e);
                                }
                            }
                        });
            }

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
        // 중복 검사 (비어있는 값 확인)
        Optional<Article> optionalArticle = articleRepository.findByOriginalUrl(dto.getOriginalUrl());

        if (optionalArticle.isPresent()) {
            Article existingArticle = optionalArticle.get();

            boolean hasTranslatedContent = existingArticle.getTranslatedContent() != null;
            boolean hasTranslatedTitle = existingArticle.getTranslatedTitle() != null;
            boolean hasSummary = existingArticle.getSummary() != null;

            if (hasTranslatedContent && hasTranslatedTitle && hasSummary) {
                // 모두 완료된 기사, 스킵
                log.info("완료된 중복 기사 스킵: {}", dto.getTitle());
                return false;
            }

            // 부족한 부분이 있음 -> 후속 작업 필요
            log.info("기존 기사에 번역/요약 미완료, 후속 작업 진행: {}", dto.getTitle());
            return true;
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

    /**
     * 관리자 요청을 받아 새로운 크롤링 Job을 DB에 저장함
     *
     * @param request Job 생성 요청 DTO
     * @return 생성된 CrawlingJob의 ID
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-01-26
     */
    @Transactional
    public Long createCrawlingJob(CrawlingJobRequestDto request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리 ID입니다: " + request.getCategoryId()));

        // URL 결정 로직 (커스텀 URL 우선 -> 없으면 자동 생성)
        String finalUrl = request.getCustomTargetUrl();

        if (finalUrl == null || finalUrl.isEmpty()) {
            // URL도 없고 리그도 없으면 에러
            if (request.getLeague() == null) {
                throw new IllegalArgumentException("리그를 선택하거나 타겟 URL을 입력해야 합니다.");
            }
            // 소스 + 리그 조합으로 URL 자동 생성
            finalUrl = generateUrl(request.getSourceName(), request.getLeague());
        }

        String cron = request.getCronExpression();
        if (cron == null || cron.isEmpty()) {
            cron = "0 0 * * * *"; // 기본 1시간
        }

        CrawlingJob newJob = CrawlingJob.builder()
                .sourceName(request.getSourceName())
                .jobName(request.getJobName())
                .targetUrl(finalUrl)
                .category(category)
                .cronExpression(cron)
                .isActive(true)
                .build();

        if (jobRepository.existsByJobName(request.getJobName())) {
            throw new IllegalArgumentException("이미 존재하는 Job 이름입니다: " + request.getJobName());
        }

        CrawlingJob savedJob = jobRepository.save(newJob);
        log.info("새로운 크롤링 Job 생성 완료: {} (ID: {}, URL: {})", savedJob.getJobName(), savedJob.getId(), finalUrl);

        return savedJob.getId();
    }

    /**
     * 모든 크롤링 작업 목록을 조회함
     */
    @Transactional(readOnly = true)
    public List<CrawlingJob> getAllJobs() {
        return jobRepository.findAll();
    }

    /**
     * 크롤링 작업의 정보를 수정함 (스케줄 변경, 활성/비활성 상태 변경)
     * 변경 감지(Dirty Checking)를 이용해 DB 업데이트 수행
     * * @return 수정된 CrawlingJob 엔티티 (스케줄러 반영을 위해 반환)
     */
    @Transactional
    public CrawlingJob updateCrawlingJob(Long jobId, CrawlingJobUpdateDto dto) {
        CrawlingJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        // Cron 변경 요청이 있으면 업데이트
        if (dto.getCronExpression() != null && !dto.getCronExpression().isEmpty()) {
            job.updateCron(dto.getCronExpression()); // Entity에 해당 메서드 혹은 setter 필요
        }

        // 활성/비활성 상태 변경 요청이 있으면 업데이트
        if (dto.getIsActive() != null) {
            job.updateActive(dto.getIsActive());
        }

        return job;
    }

    /**
     * 크롤링 작업을 삭제함
     */
    @Transactional
    public void deleteCrawlingJob(Long jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new IllegalArgumentException("Job not found: " + jobId);
        }

        logRepository.deleteByCrawlingJobId(jobId);
        trackingRepository.deleteByJobId(jobId);
        jobRepository.deleteById(jobId);

        log.info("Job ID {} 및 관련 로그/추적 데이터 삭제 완료", jobId);
    }

    @Transactional(readOnly = true)
    public Page<CrawlingLogResponseDto> getCrawlingLogs(Long jobId, Pageable pageable) {
        Page<CrawlingLog> logPage;

        if (jobId != null) {
            logPage = logRepository.findByCrawlingJobId(jobId, pageable);
        } else {
            logPage = logRepository.findAll(pageable);
        }

        return logPage.map(log -> CrawlingLogResponseDto.builder()
                .logId(log.getId())
                .jobId(log.getCrawlingJob().getId())
                .jobName(log.getCrawlingJob().getJobName())
                .status(log.getStatus())
                .collectedCount(log.getCollectedCount())
                .message(log.getMessage())
                .startTime(log.getCreatedDate())
                .endTime(log.getUpdatedDate())

                // 소요 시간 계산
                .durationSeconds(log.getUpdatedDate() != null
                        ? java.time.Duration.between(log.getCreatedDate(), log.getUpdatedDate()).getSeconds()
                        : 0)
                .build());
    }

    private String generateUrl(SourceName sourceName, EspnLeague league) {
        if (sourceName == SourceName.ESPN) {
            // ESPN은 API 방식 (기존 로직)
            return ESPN_BASE_URL + league.getApiPath() + "/news";
        }

        else if (sourceName == SourceName.SKY_SPORTS) {
            // Sky Sports는 HTML 페이지 방식
            // EspnLeague Enum을 재활용하되, Sky Sports에 맞는 URL로 매핑
            switch (league) {
                case EPL:
                    return "https://www.skysports.com/premier-league-news";
                case NBA:
                    return "https://www.skysports.com/nba/news";
                case BUNDESLIGA:
                    return "https://www.skysports.com/bundesliga-news";
                case LALIGA:
                    return "https://www.skysports.com/la-liga-news";
                case MLB:
                    return "https://www.skysports.com/mlb/news";
                default:
                    throw new IllegalArgumentException("Sky Sports에서 아직 지원하지 않는 리그입니다: " + league);
            }
        }

        throw new IllegalArgumentException("URL 생성 로직이 정의되지 않은 소스입니다: " + sourceName);
    }
}