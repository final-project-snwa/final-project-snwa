package com.team.snwa.snwabackend.domain.translation.client;

import com.deepl.api.TextResult;
import com.deepl.api.Translator;
import com.team.snwa.snwabackend.domain.translation.config.DeepLConfig;
import com.team.snwa.snwabackend.domain.translation.dto.request.CrawledArticleRequestDto;
import com.team.snwa.snwabackend.domain.translation.dto.response.TranslatedArticleResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeepLClient implements TranslationClient {

    private final DeepLConfig deepLConfig;
    private Translator translator;

    // DeepL 공식 Java SDK에 포함된 클래스 -> translateText() 메서드 등으로 간단히 번역 요청할 수 있게 해줌
    private Translator getTranslator() {
        if (translator == null) {
            translator = new Translator(deepLConfig.getApiKey());
        }
        return translator;
    }

    @Override
    public TranslatedArticleResponseDto translate(CrawledArticleRequestDto request, String targetLang) {
        try {
            Translator translator = getTranslator();

            // 1. 제목과 본문을 합쳐서 번역 (문맥 유지 및 일관성 향상, API 호출 횟수 절약)
            String separator = "<||SMA_SEPARATOR||>";
            String title = request.getTitle() != null ? request.getTitle() : "";
            String content = request.getContent() != null ? request.getContent() : "";

            // 구분자 앞뒤에 줄바꿈을 넣어 문맥 분리 효과도 줌
            String combinedText = title + "\n" + separator + "\n" + content;
            // 전체 번역 요청
            String translatedCombined = translateText(translator, combinedText, targetLang);
            String translatedTitle = "";
            String translatedContent = "";

            if (translatedCombined != null) {
                // 2. 특수 구분자를 기준으로 다시 분리
                // 정규식 특수문자([]) 이스케이프 처리 필요
                String[] parts = translatedCombined.split("<\\|\\|SMA_SEPARATOR\\|\\|>", 2);

                if (parts.length >= 2) {
                    translatedTitle = parts[0].trim();
                    translatedContent = parts[1].trim();
                } else {
                    // 혹시라도 구분자가 변형되었을 경우를 대비해 기존 줄바꿈 로직도 유지하거나,
                    // 로그를 남기고 전체를 본문에 넣는 식의 처리가 필요할 수 있음
                    // 여기서는 일단 제목에 다 들어간 것으로 보임
                    translatedTitle = translatedCombined;
                    translatedContent = "";
                }
            }

            return TranslatedArticleResponseDto.builder()
                    .title(request.getTitle()) // 원문 제목
                    .content(request.getContent()) // 원문 내용
                    .translatedTitle(translatedTitle) // 번역된 제목
                    .translatedContent(translatedContent) // 번역된 내용
                    .authorName(request.getAuthorName())
                    .publisherName(request.getPublisherName())
                    .originalUrl(request.getOriginalUrl())
                    .build();
        } catch (Exception e) {
            log.error("번역 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("번역 실패: " + e.getMessage(), e);
        }
    }

    private String translateText(Translator translator, String text, String targetLang) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            // targetLang을 null로 설정하면 자동 감지(KO를 기본값으로 사용)
            String lang = (targetLang == null || targetLang.isBlank()) ? "KO" : targetLang;
            TextResult result = translator.translateText(text, null, lang);
            return result.getText();
        } catch (Exception e) {
            log.error("텍스트 번역 실패: {}", e.getMessage(), e);
            // 번역 실패 시 원본 텍스트 반환
            return text;
        }
    }
}