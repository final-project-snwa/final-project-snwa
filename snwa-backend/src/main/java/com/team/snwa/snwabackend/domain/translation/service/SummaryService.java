package com.team.snwa.snwabackend.domain.translation.service;

import com.team.snwa.snwabackend.domain.translation.dto.response.SummaryResponseDto;
import com.team.snwa.snwabackend.domain.translation.entity.ArticleTranslation;
import com.team.snwa.snwabackend.domain.translation.repository.ArticleTranslationRepository;
import com.team.snwa.snwabackend.domain.translation.client.GeminiClientManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final GeminiClientManager geminiClientManager;
    private final ArticleTranslationRepository articleTranslationRepository;
    private final ResourceLoader resourceLoader;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SummaryResponseDto summarizeArticle(Long articleId) {
        log.info("기사 요약 시작: articleId={}", articleId);

        // 번역 데이터 조회 (KO)
        ArticleTranslation translation = articleTranslationRepository.findByArticleIdAndLanguage(articleId, "KO")
                .orElseThrow(() -> new RuntimeException("번역 데이터를 찾을 수 없습니다: articleId=" + articleId));

        // 번역된 내용이 없으면 예외 처리
        if (translation.getTranslatedContent() == null || translation.getTranslatedContent().isBlank()) {
            throw new RuntimeException("번역된 내용이 없습니다. 먼저 번역을 진행해주세요.");
        }

        if (translation.getSummary() != null && !translation.getSummary().isBlank()) {
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

    /**
     * 요약본이 없을 때만 Gemini API를 호출하여 요약을 생성하고 저장합니다.
     *
     * @param translation 번역 엔티티 (요약 여부 확인 및 저장 대상)
     * @param targetLang  요약 출력 언어 코드 (예: "KO", "EN", "JA", "ZH")
     * @return 요약 문자열 — DB에 이미 있으면 바로 반환, API 실패 시 null 반환 (QUOTA 초과는 예외 전파)
     */
    @Transactional
    public String summarizeIfNeeded(ArticleTranslation translation, String targetLang) {
        if (translation.getSummary() != null && !translation.getSummary().isBlank()) {
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
        String langName = toLanguageName(targetLang);   //Ex: 日本語
        String promptTemplate = loadPromptTemplate();
        String prompt = promptTemplate
                .replace("{targetLanguage}", langName)
                .replace("{translatedContent}", translatedContent);
        return geminiClientManager.generate(prompt);
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
            //notes.md 파일 참조  classpath = src/main/resources
            Resource resource = resourceLoader.getResource("classpath:prompts/summary-prompt.txt");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("프롬프트 템플릿 로드 실패: {}", e.getMessage(), e);
            return "다음 기사 내용을 3줄로 {targetLanguage}로 요약해주세요.\n\n기사 내용:\n{translatedContent}";
        }
    }
}