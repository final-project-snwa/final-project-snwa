package com.team.snwa.snwabackend.domain.translation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// TranslationService에서 DeepL API 호출 시 번역할 원문 데이터를 담아 전달하는 요청 객체
public class CrawledArticleRequestDto {
    private String title;
    private String content;
    private String authorName;
    private String publisherName;
    private String originalUrl;
}
