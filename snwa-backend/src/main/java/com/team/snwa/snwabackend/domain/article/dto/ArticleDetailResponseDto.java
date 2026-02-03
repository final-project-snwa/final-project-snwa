package com.team.snwa.snwabackend.domain.article.dto;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ArticleDetailResponseDto {
    private Long id;
    private String title;
    private String translatedTitle;
    private String content;
    private String translatedContent;
    private String summary;
    private String originalUrl;
    private String categoryName;
    private String authorName;
    private String publisherName;
    private String imageUrl;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private boolean isBookmarked;
    private Long clickCount;

    public static ArticleDetailResponseDto from(Article article) {
        return from(article, false);
    }

    public static ArticleDetailResponseDto from(Article article, boolean isBookmarked) {
        return from(article, isBookmarked, article.getClickCount());
    }

    public static ArticleDetailResponseDto from(Article article, boolean isBookmarked, Long displayedClickCount) {
        return ArticleDetailResponseDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .translatedTitle(article.getTranslatedTitle())
                .content(article.getContent())
                .translatedContent(article.getTranslatedContent())
                .summary(article.getSummary())
                .originalUrl(article.getOriginalUrl())
                .categoryName(article.getCategory() != null 
                        ? article.getCategory().getCategoryName().name() 
                        : null)
                .authorName(article.getAuthorName())
                .publisherName(article.getPublisherName())
                .imageUrl(article.getImageUrl())
                .createdDate(article.getCreatedDate())
                .updatedDate(article.getUpdatedDate())
                .isBookmarked(isBookmarked)
                .clickCount(displayedClickCount != null ? displayedClickCount : 0L)
                .build();
    }
}
