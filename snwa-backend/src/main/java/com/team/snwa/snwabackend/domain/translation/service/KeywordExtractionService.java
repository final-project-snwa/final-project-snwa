package com.team.snwa.snwabackend.domain.translation.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.entity.ArticleTag;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.article.repository.ArticleTagRepository;
import com.team.snwa.snwabackend.domain.interest.entity.InterestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordExtractionService {

    private final ChatClient.Builder chatClientBuilder;
    private final ArticleRepository articleRepository;
    private final ArticleTagRepository articleTagRepository;
    private final ResourceLoader resourceLoader;
    private final com.team.snwa.snwabackend.domain.interest.service.InterestService interestService;
    private final com.team.snwa.snwabackend.domain.notification.service.NotificationService notificationService;

    // AI 응답에서 키워드(타입) 형식을 파싱하기 위한 정규식
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("([^,()]+)\\(([^)]+)\\)");

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void extractKeywords(Long articleId) {
        log.info("기사 키워드 추출 시작: articleId={}", articleId);

        // DB에서 Article 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("기사를 찾을 수 없습니다: " + articleId));

        // 이미 태그가 하나라도 있으면 추출 안 함
        if (articleTagRepository.existsByArticleId(articleId)) {
            log.info("이미 태그가 존재하여 키워드 추출을 건너뜁니다: articleId={}", articleId);
            return;
        }

        // 번역된 내용이 없으면 예외 처리
        if (article.getTranslatedContent() == null || article.getTranslatedContent().trim().isEmpty()) {
            throw new RuntimeException("번역된 내용이 없습니다. 먼저 번역을 진행해주세요.");
        }

        // 프롬프트 로드 및 호출
        String promptTemplate = loadPromptTemplate();
        String prompt = promptTemplate.replace("{translatedContent}", article.getTranslatedContent());

        // Gemini로 키워드 추출 요청
        ChatClient chatClient = chatClientBuilder.build();
        String keywordsResponse = chatClient.prompt(prompt)
                .call()
                .content();

        // 키워드 파싱 - Map<키워드, 타입>
        Map<String, InterestType> typedKeywords = parseTypedKeywords(keywordsResponse);
        List<String> keywordList = new ArrayList<>(typedKeywords.keySet());

        // ArticleTag 엔티티 생성 및 저장
        for (String keyword : keywordList) {
            if (!keyword.isEmpty()) {
                ArticleTag articleTag = ArticleTag.builder()
                        .article(article)
                        .tagName(keyword)
                        .build();
                articleTagRepository.save(articleTag);
            }
        }

        log.info("기사 키워드 추출 완료 및 저장: articleId={}, keywords={}", articleId, typedKeywords);

        // 관심사 구독 유저에게 알림 발송
        try {
            // 타입 정보와 함께 구독자 조회 및 태그 자동 등록
            List<com.team.snwa.snwabackend.domain.user.entity.User> interestedUsers = interestService
                    .findSubscribersForTypedTags(typedKeywords);

            if (!interestedUsers.isEmpty()) {
                log.info("알림 발송 대상 유저: {}명", interestedUsers.size());

                String articleTitle = article.getTranslatedTitle() != null && !article.getTranslatedTitle().isEmpty()
                        ? article.getTranslatedTitle()
                        : article.getTitle();

                String notificationMessage = "새로운 관심 기사가 등록되었습니다: " + articleTitle;

                for (com.team.snwa.snwabackend.domain.user.entity.User user : interestedUsers) {
                    notificationService.createNotification(user, article, notificationMessage);
                }
            }
        } catch (Exception e) {
            log.error("알림 발송 중 오류 발생: articleId={}", articleId, e);
            // 알림 실패가 키워드 추출 실패로 이어지지 않게 예외 처리
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
}