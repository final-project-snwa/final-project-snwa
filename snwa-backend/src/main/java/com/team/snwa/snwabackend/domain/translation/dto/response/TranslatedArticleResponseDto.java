package com.team.snwa.snwabackend.domain.translation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslatedArticleResponseDto {
    private String title;
    private String content;
    private String translatedTitle;
    private String translatedContent;
    private String authorName;
    private String publisherName;
    private String originalUrl;
}
