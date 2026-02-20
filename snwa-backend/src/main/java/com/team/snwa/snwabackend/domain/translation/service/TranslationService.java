package com.team.snwa.snwabackend.domain.translation.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.entity.ArticleTag;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.article.repository.ArticleTagRepository;
import com.team.snwa.snwabackend.domain.translation.client.TranslationClient;
import com.team.snwa.snwabackend.domain.translation.dto.request.CrawledArticleRequestDto;
import com.team.snwa.snwabackend.domain.translation.dto.response.TranslatedArticleResponseDto;
import com.team.snwa.snwabackend.domain.translation.entity.ArticleTranslation;
import com.team.snwa.snwabackend.domain.translation.entity.TranslationAccessLog;
import com.team.snwa.snwabackend.domain.translation.repository.ArticleTranslationRepository;
import com.team.snwa.snwabackend.domain.translation.repository.TranslationAccessLogRepository;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.entity.enums.UserRole;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.domain.wallet.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TranslationService {

    private final TranslationClient translationClient;
    private final ArticleRepository articleRepository;
    private final ArticleTranslationRepository articleTranslationRepository;
    private final SummaryService summaryService;
    private final KeywordExtractionService keywordExtractionService;
    private final TranslationAccessLogRepository translationAccessLogRepository;
    private final WalletTransactionService walletTransactionService;
    private final UserRepository userRepository;
    private final ArticleTagRepository articleTagRepository;

    // 번역본을 볼 수 있는 권한 체크
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TranslatedArticleResponseDto getTranslation(Long userId, Long articleId, String language) {

        // 언어 기본값 KO
        String targetLang = (language == null || language.isBlank()) ? "KO" : language.toUpperCase();

        // 1. 접근 권한 확인 (기사 + 언어 단위)
        boolean hasAccess = translationAccessLogRepository.existsByUserIdAndArticleIdAndLanguage(userId, articleId,
                targetLang);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!hasAccess && user.getRole() != UserRole.ADMIN) {
            // 2. 권한 없으면 코인 차감
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new RuntimeException("기사를 찾을 수 없습니다."));

            // 코인 차감 (1코인)
            String externalRef = "TRANS_" + articleId + "_" + targetLang + "_" + System.currentTimeMillis();
            walletTransactionService.spend(user, 1L, externalRef);

            // 접근 로그 저장
            TranslationAccessLog log = TranslationAccessLog.builder()
                    .user(user)
                    .article(article)
                    .language(targetLang)
                    .build();
            translationAccessLogRepository.save(log);
        }

        // 3. 번역 데이터 조회 또는 생성
        return articleTranslationRepository.findByArticleIdAndLanguage(articleId, targetLang)
                .map(t -> {
                    boolean updated = false;

                    // 요약이 비어있으면 채워넣기
                    String summary = t.getSummary();
                    if (summary == null || summary.isBlank()) {
                        try {
                            summary = summaryService.generateSummary(t.getTranslatedContent(), targetLang);
                            t.updateSummary(summary);
                            updated = true;
                        } catch (Exception e) {
                            log.error("요약기능 실패(Skipped): {}", e.getMessage());
                        }
                    }

                    // [수정] 태그 조회: ArticleTag 테이블에서 가져옴
                    List<String> tags = articleTagRepository.findByArticleIdAndLanguage(articleId, targetLang)
                            .stream()
                            .map(ArticleTag::getTagName)
                            .toList();

                    // 태그가 없으면 새로 추출 (보수 로직)
                    if (tags.isEmpty()) {
                        try {
                            tags = keywordExtractionService.extractKeywordsToList(t.getTranslatedContent(), targetLang);
                            if (!tags.isEmpty()) {
                                // ArticleTag 테이블에 저장
                                for (String tagName : tags) {
                                    articleTagRepository.save(ArticleTag.builder()
                                            .article(t.getArticle())
                                            .tagName(tagName)
                                            .language(targetLang)
                                            .build());
                                }
                            }
                        } catch (Exception e) {
                            log.error("태그기능 실패(Skipped): {}", e.getMessage());
                        }
                    }

                    if (updated) {
                        articleTranslationRepository.save(t);
                    }

                    return TranslatedArticleResponseDto.builder()
                            .title(t.getArticle().getTitle())
                            .content(t.getArticle().getContent())
                            .translatedTitle(t.getTranslatedTitle())
                            .translatedContent(t.getTranslatedContent())
                            .summary(summary)
                            .tags(tags)
                            .authorName(t.getArticle().getAuthorName())
                            .publisherName(t.getArticle().getPublisherName())
                            .originalUrl(t.getArticle().getOriginalUrl())
                            .build();
                })
                .orElseGet(() -> {
                    // 없으면 새로 번역 요청
                    return translateAndSave(articleId, targetLang);
                });
    }

    // 실제 번역 API 호출 및 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TranslatedArticleResponseDto translateAndSave(Long articleId, String targetLang) {

        // 1. 이미 번역된 데이터가 DB에 있는지 확인 (API 호출 방지)
        var existingTranslation = articleTranslationRepository.findByArticleIdAndLanguage(articleId, targetLang);

        if (existingTranslation.isPresent()) {
            ArticleTranslation t = existingTranslation.get();
            boolean updated = false; // 변경 사항이 있는지 체크
            // 1-1. 요약이 비어있으면 채워넣기 (보수 로직)
            if (t.getSummary() == null || t.getSummary().isBlank()) {
                try {
                    String summary = summaryService.generateSummary(t.getTranslatedContent(), targetLang);
                    t.updateSummary(summary);
                    updated = true;
                    log.info("기존 번역본에 요약이 없어 새로 생성했습니다: articleId={}", articleId);
                } catch (Exception e) {
                    log.error("요약 생성 실패(Skipped): {}", e.getMessage());
                }
            }

            // 1-2. 태그가 비어있으면 채워넣기 (보수 로직)
            // [수정] ArticleTag 테이블에서 조회
            List<String> tags = articleTagRepository.findByArticleIdAndLanguage(articleId, targetLang)
                    .stream()
                    .map(ArticleTag::getTagName)
                    .toList();

            if (tags.isEmpty()) {
                try {
                    // 태그가 없으면 새로 추출
                    tags = keywordExtractionService.extractKeywordsToList(t.getTranslatedContent(), targetLang);
                    if (!tags.isEmpty()) {
                        // ArticleTag 테이블에 저장
                        for (String tagName : tags) {
                            articleTagRepository.save(ArticleTag.builder()
                                    .article(t.getArticle())
                                    .tagName(tagName)
                                    .language(targetLang)
                                    .build());
                        }
                    }
                } catch (Exception e) {
                    log.error("태그기능 실패(Skipped): {}", e.getMessage());
                }
            }

            // 변경사항이 있으면 저장
            if (updated) {
                articleTranslationRepository.save(t);
            }

            log.info("최종 데이터 반환 (DB 캐시 사용): articleId={}, lang={}", articleId, targetLang);
            return TranslatedArticleResponseDto.builder()
                    .title(t.getArticle().getTitle())
                    .content(t.getArticle().getContent())
                    .translatedTitle(t.getTranslatedTitle())
                    .translatedContent(t.getTranslatedContent())
                    .summary(t.getSummary())
                    .tags(tags)
                    .authorName(t.getArticle().getAuthorName())
                    .publisherName(t.getArticle().getPublisherName())
                    .originalUrl(t.getArticle().getOriginalUrl())
                    .build();
        }

        log.info("기사 번역/요약/태그 요청 시작: articleId={}, lang={}", articleId, targetLang);
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("기사를 찾을 수 없습니다: " + articleId));

        CrawledArticleRequestDto request = CrawledArticleRequestDto.builder()
                .title(article.getTitle())
                .content(article.getContent())
                .authorName(article.getAuthorName())
                .publisherName(article.getPublisherName())
                .originalUrl(article.getOriginalUrl())
                .build();

        // 1. 번역 실행
        TranslatedArticleResponseDto response = translationClient.translate(request, targetLang);
        String translatedContent = response.getTranslatedContent();

        // 2. 요약 생성 (번역 언어와 동일한 언어로 요약)
        String summary = null;
        try {
            if (translatedContent != null && !translatedContent.trim().isEmpty()) {
                summary = summaryService.generateSummary(translatedContent, targetLang);
            } else {
                log.warn("번역된 내용이 비어있어 요약을 생략합니다. articleId={}", articleId);
            }
        } catch (Exception e) {
            if (e instanceof com.team.snwa.snwabackend.global.exception.CustomException &&
                    ((com.team.snwa.snwabackend.global.exception.CustomException) e)
                            .getErrorCode() == com.team.snwa.snwabackend.global.exception.ErrorCode.AI_API_QUOTA_EXCEEDED) {
                throw e;
            }
            log.error("요약 생성 실패: {}", e.getMessage());
        }

        // 3. 키워드 추출
        List<String> tags = new ArrayList<>();
        try {
            tags = keywordExtractionService.extractKeywordsToList(translatedContent, targetLang);
            if (!tags.isEmpty()) {
                if (!articleTagRepository.existsByArticleIdAndLanguage(articleId, targetLang)) {
                    for (String tagName : tags) {
                        articleTagRepository.save(ArticleTag.builder()
                                .article(article)
                                .tagName(tagName)
                                .language(targetLang)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof com.team.snwa.snwabackend.global.exception.CustomException &&
                    ((com.team.snwa.snwabackend.global.exception.CustomException) e)
                            .getErrorCode() == com.team.snwa.snwabackend.global.exception.ErrorCode.AI_API_QUOTA_EXCEEDED) {
                throw e;
            }
            log.error("키워드 추출 실패: {}", e.getMessage());
        }

        // 4. 저장 (ArticleTranslation 엔티티)
        ArticleTranslation translation = ArticleTranslation.builder()
                .article(article)
                .language(targetLang)
                .translatedTitle(response.getTranslatedTitle())
                .translatedContent(translatedContent)
                .summary(summary)
                .build();

        articleTranslationRepository.save(translation);

        // 한국어일 경우 원본 Article에도 업데이트 (선택사항)
        if ("KO".equals(targetLang)) {
            article.setTranslatedTitle(response.getTranslatedTitle());
            article.setTranslatedContent(translatedContent);
            article.setSummary(summary);
            articleRepository.save(article);
        }

        log.info("번역/요약/태그 처리 완료: articleId={}, lang={}", articleId, targetLang);

        // 5. 응답 반환
        return TranslatedArticleResponseDto.builder()
                .title(article.getTitle())
                .content(article.getContent())
                .translatedTitle(response.getTranslatedTitle())
                .translatedContent(translatedContent)
                .authorName(article.getAuthorName())
                .publisherName(article.getPublisherName())
                .originalUrl(article.getOriginalUrl())
                .summary(summary)
                .tags(tags)
                .build();
    }

    @Deprecated
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TranslatedArticleResponseDto translateArticle(Long articleId) {
        return translateAndSave(articleId, "KO");
    }
}