package com.team.snwa.snwabackend.domain.translation.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.entity.ArticleTag;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.article.repository.ArticleTagRepository;
import com.team.snwa.snwabackend.domain.interest.entity.InterestType;
import com.team.snwa.snwabackend.domain.notification.event.ArticleReadyForNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.context.ApplicationEventPublisher;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordExtractionService {

    private final ApplicationEventPublisher eventPublisher;
    private final ChatClient.Builder chatClientBuilder;
    private final ArticleRepository articleRepository;
    private final ArticleTagRepository articleTagRepository;
    private final ResourceLoader resourceLoader;
    private final com.team.snwa.snwabackend.domain.interest.service.InterestService interestService;

    // AI 응답에서 키워드(타입) 형식을 파싱하기 위한 정규식
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("([^,()]+)\\(([^)]+)\\)");

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void extractKeywords(Long articleId, String targetLang) {
        log.info("기사 키워드 추출 시작: articleId={}, lang={}", articleId, targetLang);

        // DB에서 Article 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("기사를 찾을 수 없습니다: " + articleId));

        // 이미 태그가 하나라도 있으면 추출 안 함
        if (articleTagRepository.existsByArticleIdAndLanguage(articleId, targetLang)) {
            log.info("이미 해당 언어({})의 태그가 존재하여 키워드 추출을 건너뜁니다: articleId={}", targetLang, articleId);
            return;
        }

        // 번역된 내용이 없으면 예외 처리
        if (article.getTranslatedContent() == null || article.getTranslatedContent().trim().isEmpty()) {
            throw new RuntimeException("번역된 내용이 없습니다. 먼저 번역을 진행해주세요.");
        }

        // 키워드 파싱 - Map<키워드, 타입>
        Map<String, InterestType> typedKeywords = extractKeywordsFromContent(article.getTranslatedContent(),
                targetLang);
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
                        article.getId(),
                        typedKeywords));
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
        ChatClient chatClient = chatClientBuilder.build();
        try {
            String keywordsResponse = chatClient.prompt(prompt).call().content();
            return parseTypedKeywords(keywordsResponse);
        } catch (Exception e) {
            log.error("AI 키워드 추출 중 오류 발생: {}", e.getMessage(), e);
            // Gemini 할당량 초과(429, Resource exhausted) 확인
            if (e.getMessage().contains("429") || e.getMessage().contains("Resource exhausted")) {
                throw new com.team.snwa.snwabackend.global.exception.CustomException(
                        com.team.snwa.snwabackend.global.exception.ErrorCode.AI_API_QUOTA_EXCEEDED);
            }
            throw new com.team.snwa.snwabackend.global.exception.CustomException(
                    com.team.snwa.snwabackend.global.exception.ErrorCode.AI_API_ERROR);
        }
    }

    /**
     * AI 응답에서 "키워드(타입)" 형식을 파싱하여 Map으로 반환
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