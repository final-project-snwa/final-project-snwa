package com.team.snwa.snwabackend.domain.translation.client;

import com.deepl.api.Translator;
import com.team.snwa.snwabackend.domain.translation.config.DeepLConfig;
import com.team.snwa.snwabackend.domain.translation.dto.request.CrawledArticleRequestDto;
import com.team.snwa.snwabackend.domain.translation.dto.response.TranslatedArticleResponseDto;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeepLClient implements TranslationClient {

    private final DeepLConfig deepLConfig;
    private final AtomicInteger currentKeyIndex = new AtomicInteger(0);

    @Override
    public TranslatedArticleResponseDto translate(CrawledArticleRequestDto request, String targetLang) {
        List<String> keys = deepLConfig.getApiKeys();
        if (keys.isEmpty()) {
            throw new CustomException(ErrorCode.TRANSLATION_API_ERROR);
        }

        int startIndex = currentKeyIndex.get();
        for (int i = 0; i < keys.size(); i++) {
            int index = (startIndex + i) % keys.size();
            try {
                TranslatedArticleResponseDto result = doTranslate(keys.get(index), request, targetLang);
                currentKeyIndex.set(index);
                return result;
            } catch (CustomException e) {
                if (e.getErrorCode() == ErrorCode.TRANSLATION_API_QUOTA_EXCEEDED) {
                    log.warn("DeepL 키 [{}] quota 초과, 다음 키로 전환 ({}/{})", index, i + 1, keys.size());
                    currentKeyIndex.set((index + 1) % keys.size());
                    continue;
                }
                throw e;
            }
        }

        log.error("모든 DeepL API 키 quota 초과");
        throw new CustomException(ErrorCode.TRANSLATION_API_QUOTA_EXCEEDED);
    }

    private TranslatedArticleResponseDto doTranslate(String apiKey, CrawledArticleRequestDto request, String targetLang) {
        try {
            Translator translator = new Translator(apiKey);
            String lang = (targetLang == null || targetLang.isBlank()) ? "KO" : targetLang;

            String separator = "<||SMA_SEPARATOR||>";
            String title = request.getTitle() != null ? request.getTitle() : "";
            String content = request.getContent() != null ? request.getContent() : "";
            String combinedText = title + "\n" + separator + "\n" + content;

            String translatedCombined = translator.translateText(combinedText, null, lang).getText();

            String translatedTitle = "";
            String translatedContent = "";
            if (translatedCombined != null) {
                String[] parts = translatedCombined.split("<\\|\\|SMA_SEPARATOR\\|\\|>", 2);
                if (parts.length >= 2) {
                    translatedTitle = parts[0].trim();
                    translatedContent = parts[1].trim();
                } else {
                    translatedTitle = translatedCombined;
                }
            }

            return TranslatedArticleResponseDto.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .translatedTitle(translatedTitle)
                    .translatedContent(translatedContent)
                    .authorName(request.getAuthorName())
                    .publisherName(request.getPublisherName())
                    .originalUrl(request.getOriginalUrl())
                    .build();

        } catch (Exception e) {
            log.error("번역 중 오류 발생: {}", e.getMessage(), e);
            if (e.getMessage() != null &&
                    (e.getMessage().contains("Quota exceeded") || e.getMessage().contains("456"))) {
                throw new CustomException(ErrorCode.TRANSLATION_API_QUOTA_EXCEEDED);
            }
            throw new CustomException(ErrorCode.TRANSLATION_API_ERROR);
        }
    }
}