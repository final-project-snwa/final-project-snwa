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
    private String articleUrl;
    private String publisher;
    private String author;
    private String imageUrl;
}
