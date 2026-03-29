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

    /**
     * DeepL API를 사용하여 기사를 번역
     *
     * @param request    번역할 원문 데이터 (title, content, authorName, publisherName, originalUrl)
     * @param targetLang 번역 대상 언어 코드 (예: "KO", "EN", "JA", "ZH")
     * @return 번역된 제목·본문이 담긴 응답 DTO
     */
    @Override
    public TranslatedArticleResponseDto translate(CrawledArticleRequestDto request, String targetLang) {
        List<String> keys = deepLConfig.getApiKeys();   //keys = ["key-A", "key-B", "key-C"] 리스트 형식
        if (keys.isEmpty()) {
            throw new CustomException(ErrorCode.TRANSLATION_API_ERROR);
        }

        int startIndex = currentKeyIndex.get(); //현재 사용중인 key 번호 가져오기
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

    /**
     * 단일 API 키로 실제 DeepL 번역을 수행합니다.
     *
     * @param apiKey     현재 사용할 DeepL API 키 (예: "key-A", "key-B", "key-C" 중 하나)
     * @param request    번역할 원문 데이터 (title, content, authorName, publisherName, originalUrl)
     * @param targetLang 번역 대상 언어 코드 (예: "KO", "EN", "JA", "ZH")
     * @return 번역된 제목·본문이 담긴 응답 DTO
     */
    private TranslatedArticleResponseDto doTranslate(String apiKey, CrawledArticleRequestDto request, String targetLang) {
        try {
            Translator translator = new Translator(apiKey);
            String lang = (targetLang == null || targetLang.isBlank()) ? "KO" : targetLang;

            String separator = "<||SMA_SEPARATOR||>";
            String title = request.getTitle() != null ? request.getTitle() : "";
            String content = request.getContent() != null ? request.getContent() : "";
            String combinedText = title + "\n" + separator + "\n" + content;

            //translateText()는 DeepL SDK 제공 메서드 / 파라미터: (번역할 텍스트, 원본언어:null(자동감지), 번역할 언어:targetLang)
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