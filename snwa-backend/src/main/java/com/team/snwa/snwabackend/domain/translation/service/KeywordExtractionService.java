package com.team.snwa.snwabackend.domain.translation.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.entity.ArticleTag;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.article.repository.ArticleTagRepository;
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
import java.util.Arrays;
import java.util.List;
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

        // 87번째 코드 호출 (규칙이 작성된 프롬프트 호출)
        String promptTemplate = loadPromptTemplate();
        String prompt = promptTemplate.replace("{translatedContent}", article.getTranslatedContent());

        // Gemini로 키워드 추출 요청
        ChatClient chatClient = chatClientBuilder.build();
        String keywordsResponse = chatClient.prompt(prompt)
                .call()
                .content();

        // 키워드 파싱 (쉼표로 구분)
        List<String> keywords = parseKeywords(keywordsResponse);

        // ArticleTag 엔티티 생성 및 저장
        for (String keyword : keywords) {
            String trimmedKeyword = keyword.trim(); // 양쪽 끝 공백 제거
            if (!trimmedKeyword.isEmpty()) {
                ArticleTag articleTag = ArticleTag.builder()
                        .article(article)
                        .tagName(trimmedKeyword)
                        .build();
                articleTagRepository.save(articleTag);
            }
        }

        log.info("기사 키워드 추출 완료 및 저장: articleId={}, keywords={}", articleId, keywords);

        // 관심사 구독 유저에게 알림 발송
        try {
            List<com.team.snwa.snwabackend.domain.user.entity.User> interestedUsers = interestService
                    .findSubscribersForTags(keywords);
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

    private List<String> parseKeywords(String keywordsResponse) {
        // 응답에서 키워드만 추출 (앞뒤 공백 제거, 빈 문자열 제외)
        return Arrays.stream(keywordsResponse.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String loadPromptTemplate() {
        try {
            Resource resource = resourceLoader.getResource("classpath:prompts/translation-keyword-tag-prompt.txt");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("프롬프트 템플릿 로드 실패: {}", e.getMessage(), e);
            // 기본 프롬프트 반환
            return "다음 기사 내용에서 중요한 키워드를 쉼표로 구분하여 추출해주세요.\n\n기사 내용:\n{translatedContent}";
        }
    }
}