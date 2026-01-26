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
    public TranslatedArticleResponseDto translate(CrawledArticleRequestDto request) {
        try {
            Translator translator = getTranslator();

            // 제목 번역
            String translatedTitle = translateText(translator, request.getTitle());

            // 본문 번역
            String translatedContent = translateText(translator, request.getContent());

            return TranslatedArticleResponseDto.builder()
                    .title(request.getTitle())  //원문 제목
                    .content(request.getContent())  //원문 내용
                    .translatedTitle(translatedTitle) // 번역된 제목
                    .translatedContent(translatedContent)  // 번역된 내용
                    .authorName(request.getAuthorName())
                    .publisherName(request.getPublisherName())
                    .originalUrl(request.getOriginalUrl())
                    .build();
        } catch (Exception e) {
            log.error("번역 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("번역 실패: " + e.getMessage(), e);
        }
    }

    private String translateText(Translator translator, String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            // sourceLang을 null로 설정하면 자동 감지
            TextResult result = translator.translateText(text, null, "KO");
            return result.getText();
        } catch (Exception e) {
            log.error("텍스트 번역 실패: {}", e.getMessage(), e);
            // 번역 실패 시 원본 텍스트 반환
            return text;
        }
    }
}