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
    private String imageUrl;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private boolean isBookmarked;
    private Long clickCount;

    public static ArticleListResponseDto from(Article article) {
        return from(article, null, false);
    }

    public static ArticleListResponseDto from(Article article, boolean isBookmarked) {
        return from(article, null, isBookmarked);
    }

    public static ArticleListResponseDto from(Article article,
            com.team.snwa.snwabackend.domain.translation.entity.ArticleTranslation translation, boolean isBookmarked) {
        return ArticleListResponseDto.builder()
                .id(article.getId())
                .title(article.getTitle()) // 원문 제목 (항상 존재)
                .translatedTitle(translation != null ? translation.getTranslatedTitle() : article.getTitle()) // 번역 없으면
                                                                                                              // 원문
                .summary(translation != null ? translation.getSummary() : null) // 번역 없으면 요약 없음
                .categoryName(article.getCategory() != null
                        ? article.getCategory().getCategoryName().name()
                        : null)
                .authorName(article.getAuthorName())
                .publisherName(article.getPublisherName())
                .imageUrl(article.getImageUrl())
                .createdDate(article.getCreatedDate())
                .updatedDate(article.getUpdatedDate())
                .isBookmarked(isBookmarked)
                .clickCount(article.getClickCount() != null ? article.getClickCount() : 0L)
                .build();
    }
}
