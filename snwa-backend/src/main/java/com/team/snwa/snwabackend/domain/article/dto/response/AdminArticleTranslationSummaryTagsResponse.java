package com.team.snwa.snwabackend.domain.article.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminArticleTranslationSummaryTagsResponse {

    private final Long id;
    private final String translatedTitle;
    private final String translatedContent;
    private final String summary;
    private final List<String> tagNames;
}