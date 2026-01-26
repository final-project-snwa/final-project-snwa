package com.team.snwa.snwabackend.domain.article.dto;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ArticleListResponseDto {
    private Long id;
    private String title;
    private String translatedTitle;
    private String summary;
    private String categoryName;
    private String authorName;
    private String publisherName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static ArticleListResponseDto from(Article article) {
        return ArticleListResponseDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .translatedTitle(article.getTranslatedTitle())
                .summary(article.getSummary())
                .categoryName(article.getCategory() != null 
                        ? article.getCategory().getCategoryName().name() 
                        : null)
                .authorName(article.getAuthorName())
                .publisherName(article.getPublisherName())
                .createdDate(article.getCreatedDate())
                .updatedDate(article.getUpdatedDate())
                .build();
    }
}
