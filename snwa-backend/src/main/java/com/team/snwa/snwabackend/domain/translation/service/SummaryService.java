package com.team.snwa.snwabackend.domain.translation.service;

import com.team.snwa.snwabackend.domain.translation.dto.response.SummaryResponseDto;
import com.team.snwa.snwabackend.domain.translation.entity.ArticleTranslation;
import com.team.snwa.snwabackend.domain.translation.repository.ArticleTranslationRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryService {

    private final ChatClient.Builder chatClientBuilder;
    private final ArticleTranslationRepository articleTranslationRepository;
    private final ResourceLoader resourceLoader; // 파일, url등 추상적으로 불러올 수 있도록 해줌(Spring Framework 기능)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SummaryResponseDto summarizeArticle(Long articleId) {
        log.info("기사 요약 시작: articleId={}", articleId);

        // 번역 데이터 조회 (KO)
        ArticleTranslation translation = articleTranslationRepository.findByArticleIdAndLanguage(articleId, "KO")
                .orElseThrow(() -> new RuntimeException("번역 데이터를 찾을 수 없습니다: articleId=" + articleId));

        // 번역된 내용이 없으면 예외 처리
        if (translation.getTranslatedContent() == null || translation.getTranslatedContent().trim().isEmpty()) {
            throw new RuntimeException("번역된 내용이 없습니다. 먼저 번역을 진행해주세요.");
        }

        if (translation.getSummary() != null && !translation.getSummary().trim().isEmpty()) {
            log.info("이미 요약된 기사입니다. DB 저장된 값을 반환합니다: articleId={}", articleId);
            return SummaryResponseDto.builder()
                    .summary(translation.getSummary())
                    .translatedContent(translation.getTranslatedContent())
                    .build();
        }

        String summary = generateSummary(translation.getTranslatedContent(), "KO");
        translation.updateSummary(summary);

        articleTranslationRepository.save(translation);
        log.info("기사 요약 완료 및 저장: articleId={}", articleId);
        return SummaryResponseDto.builder()
                .summary(summary)
                .translatedContent(translation.getTranslatedContent())
                .build();
    }

    @Transactional
    public String summarizeIfNeeded(ArticleTranslation translation, String targetLang) {
        if (translation.getSummary() != null && !translation.getSummary().trim().isEmpty()) {
            return translation.getSummary();
        }

        try {
            String summary = generateSummary(translation.getTranslatedContent(), targetLang);
            translation.updateSummary(summary);
            articleTranslationRepository.save(translation);
            return summary;
        } catch (com.team.snwa.snwabackend.global.exception.CustomException e) {
            if (e.getErrorCode() == com.team.snwa.snwabackend.global.exception.ErrorCode.AI_API_QUOTA_EXCEEDED) {
                throw e;
            }
            log.error("요약기능 실패(Skipped): {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("요약기능 실패(Skipped): {}", e.getMessage());
            return null;
        }
    }

    /**
     * 번역된 기사 내용을 지정된 언어로 요약합니다.
     *
     * @param translatedContent 번역된 기사 본문
     * @param targetLang        요약 출력 언어 (KO, JA, EN, ZH 등)
     */
    public String generateSummary(String translatedContent, String targetLang) {
        String langName = toLanguageName(targetLang);
        String promptTemplate = loadPromptTemplate();
        String prompt = promptTemplate
                .replace("{targetLanguage}", langName)
                .replace("{translatedContent}", translatedContent);
        ChatClient chatClient = chatClientBuilder.build();
        try {
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            log.error("AI 요약 생성 중 오류 발생: {}", e.getMessage(), e);
            // Gemini 할당량 초과(429, Resource exhausted) 확인
            if (e.getMessage().contains("429") || e.getMessage().contains("Resource exhausted")) {
                throw new com.team.snwa.snwabackend.global.exception.CustomException(
                        com.team.snwa.snwabackend.global.exception.ErrorCode.AI_API_QUOTA_EXCEEDED);
            }
            throw new com.team.snwa.snwabackend.global.exception.CustomException(
                    com.team.snwa.snwabackend.global.exception.ErrorCode.AI_API_ERROR);
        }
    }

    private String toLanguageName(String targetLang) {
        if (targetLang == null || targetLang.isBlank())
            return "한국어";
        return switch (targetLang.toUpperCase()) {
            case "KO" -> "한국어";
            case "JA" -> "日本語";
            case "EN" -> "English";
            case "ZH" -> "中文";
            default -> targetLang;
        };
    }

    private String loadPromptTemplate() {
        try {
            Resource resource = resourceLoader.getResource("classpath:prompts/summary-prompt.txt"); // classpath =
                                                                                                    // src/main/resources
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("프롬프트 템플릿 로드 실패: {}", e.getMessage(), e);
            return "다음 기사 내용을 3줄로 {targetLanguage}로 요약해주세요.\n\n기사 내용:\n{translatedContent}";
        }
    }
}