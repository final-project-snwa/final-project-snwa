package com.team.snwa.snwabackend.domain.translation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawledArticleRequestDto {
    private String title;
    private String content;
    private String authorName;
    private String publisherName;
    private String originalUrl;
}
