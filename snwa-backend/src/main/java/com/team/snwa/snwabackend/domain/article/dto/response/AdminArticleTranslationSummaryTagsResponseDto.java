package com.team.snwa.snwabackend.domain.article.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminArticleTranslationSummaryTagsResponseDto {

    private final Long id;
    private final String originalTitle;

    // 번역 정보 리스트 (KO, JA, ZH 등)
    private final List<TranslationDetail> translations;

    @Getter
    @AllArgsConstructor
    public static class TranslationDetail {
        private final String language;
        private final String translatedTitle;
        private final String translatedContent;
        private final String summary;
        private final List<String> tagNames;
    }
}