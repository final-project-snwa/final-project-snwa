package com.team.snwa.snwabackend.domain.crawler.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CrawledArticleDto {
    private String title;
    private String content;
    private String originalUrl;
    private String publisherName;
    private String authorName;
    private String imageUrl;
}
