package com.team.snwa.snwabackend.domain.translation.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.entity.ArticleTag;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.article.repository.ArticleTagRepository;
import com.team.snwa.snwabackend.domain.interest.entity.InterestType;
import com.team.snwa.snwabackend.domain.notification.event.ArticleReadyForNotificationEvent;
import com.team.snwa.snwabackend.domain.translation.client.GeminiClientManager;
import com.team.snwa.snwabackend.domain.translation.entity.ArticleTranslation;
import com.team.snwa.snwabackend.domain.translation.repository.ArticleTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.context.ApplicationEventPublisher;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordExtractionService {

    private final ApplicationEventPublisher eventPublisher;
    private final GeminiClientManager geminiClientManager;
    private final ArticleRepository articleRepository;
    private final ArticleTagRepository articleTagRepository;
    private final ArticleTranslationRepository articleTranslationRepository;
    private final ResourceLoader resourceLoader;

    // AI 응답에서 키워드(타입) 형식을 파싱하기 위한 정규식
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("([^,()]+)\\(([^)]+)\\)");

    /**
     * 비동기로 키워드를 추출합니다. (크롤러 등에서 호출)
     * 4초의 대기 시간을 가져 AI API의 rate limit을 방지합니다.
     */
    @Async
    public CompletableFuture<Void> extractKeywordsAsync(Long articleId, String targetLang) {
        try {
            log.info("비동기 키워드 추출 예약됨 (4초 대기): articleId={}", articleId);
            Thread.sleep(4000); // AI API를 위한 대기 시간
            extractKeywords(articleId, targetLang);
        } catch (Exception e) {
            log.error("비동기 키워드 추출 중 오류 발생: articleId={}", articleId, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    // 번역-요약하기 버튼을 눌렀을 때 실행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void extractKeywords(Long articleId, String targetLang) {
        log.info("기사 키워드 추출 시작: articleId={}, lang={}", articleId, targetLang);

        // 이미 태그가 하나라도 있으면 추출 안 함
        if (articleTagRepository.existsByArticleIdAndLanguage(articleId, targetLang)) {
            log.info("이미 해당 언어({})의 태그가 존재하여 키워드 추출을 건너뜁니다: articleId={}", targetLang, articleId);
            return;
        }

        // 1. 기사 정보 먼저 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("기사를 찾을 수 없습니다: id=" + articleId));

        // 2. 번역 데이터 우선 조회 -> 없으면 기사 원문 조회 -> contentToExtract=번역된 본문 or 원본 본문
        String contentToExtract = articleTranslationRepository.findByArticleIdAndLanguage(articleId, targetLang)
                .map(ArticleTranslation::getTranslatedContent)
                .orElseGet(() -> {
                    log.info("번역본이 없어 원문을 사용합니다: articleId={}", articleId);
                    return article.getContent();
                });

        if (contentToExtract == null || contentToExtract.isBlank()) {
            log.warn("추출할 내용이 없습니다: articleId={}", articleId);
            return;
        }

        // 키워드 파싱 - Map<키워드, 타입>
        Map<String, InterestType> typedKeywords = extractKeywordsFromContent(contentToExtract, targetLang);
        List<String> keywordList = new ArrayList<>(typedKeywords.keySet());

        // ArticleTag 엔티티 생성 및 저장
        for (String keyword : keywordList) {
            if (!keyword.isEmpty()) {
                ArticleTag articleTag = ArticleTag.builder()
                        .article(article)
                        .tagName(keyword)
                        .language(targetLang)
                        .build();
                articleTagRepository.save(articleTag);
            }
        }

        log.info("기사 키워드 추출 완료 및 저장: articleId={}, keywords={}", articleId, typedKeywords);

        // 키워드 추출 완료 이벤트 발생
        eventPublisher.publishEvent(
                new ArticleReadyForNotificationEvent(
                        articleId,
                        typedKeywords));
    }

    @Transactional
    public List<String> extractKeywordsIfNeeded(ArticleTranslation translation, String targetLang) {
        List<String> tags = articleTagRepository
                .findByArticleIdAndLanguage(translation.getArticle().getId(), targetLang)
                .stream()
                .map(ArticleTag::getTagName)
                .toList();

        if (!tags.isEmpty()) {
            return tags;
        }

        try {
            tags = extractKeywordsToList(translation.getTranslatedContent(), targetLang);
            if (!tags.isEmpty()) {
                for (String tagName : tags) {
                    articleTagRepository.save(ArticleTag.builder()
                            .article(translation.getArticle())
                            .tagName(tagName)
                            .language(targetLang)
                            .build());
                }
            }
            return tags;
        } catch (com.team.snwa.snwabackend.global.exception.CustomException e) {
            if (e.getErrorCode() == com.team.snwa.snwabackend.global.exception.ErrorCode.AI_API_QUOTA_EXCEEDED) {
                throw e;
            }
            log.error("태그기능 실패(Skipped): {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("태그기능 실패(Skipped): {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<String> extractKeywordsToList(String translatedContent, String targetLang) {
        Map<String, InterestType> typedKeywords = extractKeywordsFromContent(translatedContent, targetLang);
        return new ArrayList<>(typedKeywords.keySet());
    }

    private Map<String, InterestType> extractKeywordsFromContent(String translatedContent, String targetLang) {
        String promptTemplate = loadPromptTemplate();

        // 언어 이름 변환 (KO -> Korean)
        String languageName = convertLangCodeToName(targetLang);

        // 프롬프트 내 {language} 치환 추가
        String prompt = promptTemplate.replace("{translatedContent}", translatedContent).replace("{language}",
                languageName);
        String keywordsResponse = geminiClientManager.generate(prompt);
        return parseTypedKeywords(keywordsResponse);
    }

    /**
     * AI 응답에서 "키워드(타입)" 형식을 파싱하여 Map으로 반환(key, value 형식)
     * 예: "호날두(Player), 맨유(Team)" -> {호날두=PLAYER, 맨유=TEAM}
     */
    private Map<String, InterestType> parseTypedKeywords(String keywordsResponse) {
        Map<String, InterestType> result = new LinkedHashMap<>();

        Matcher matcher = KEYWORD_PATTERN.matcher(keywordsResponse);
        while (matcher.find()) {
            String keyword = matcher.group(1).trim();
            String typeStr = matcher.group(2).trim().toUpperCase();

            InterestType type = mapToInterestType(typeStr);
            if (!keyword.isEmpty()) {
                result.put(keyword, type);
            }
        }

        // 파싱 실패 시 폴백: 쉼표로 분리하여 OTHER 타입으로 저장
        if (result.isEmpty()) {
            log.warn("정규식 파싱 실패, 폴백 처리: {}", keywordsResponse);
            Arrays.stream(keywordsResponse.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(keyword -> result.put(keyword, InterestType.OTHER));
        }

        return result;
    }

    /**
     * AI 응답의 타입 문자열을 InterestType enum으로 매핑
     */
    private InterestType mapToInterestType(String typeStr) {
        return switch (typeStr) {
            case "PLAYER" -> InterestType.PLAYER;
            case "SPORT" -> InterestType.SPORT;
            case "TEAM" -> InterestType.TEAM;
            case "LEAGUE" -> InterestType.LEAGUE;
            default -> InterestType.OTHER;
        };
    }

    private String loadPromptTemplate() {
        try {
            Resource resource = resourceLoader.getResource("classpath:prompts/translation-keyword-tag-prompt.txt");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("프롬프트 템플릿 로드 실패: {}", e.getMessage(), e);
            return "다음 기사 내용에서 중요한 키워드를 쉼표로 구분하여 추출해주세요.\n\n기사 내용:\n{translatedContent}";
        }
    }

    private String convertLangCodeToName(String langCode) {
        if (langCode == null)
            return "Korean";
        return switch (langCode.toUpperCase()) {
            case "KO" -> "Korean";
            case "EN" -> "English";
            case "JA" -> "Japanese";
            case "ZH" -> "Chinese";
            case "ES" -> "Spanish";
            case "FR" -> "French";
            case "DE" -> "German";
            default -> "Korean";
        };
    }
}