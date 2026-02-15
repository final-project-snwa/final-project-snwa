package com.team.snwa.snwabackend.domain.translation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private String summary;
    private List<String> tags;
}
