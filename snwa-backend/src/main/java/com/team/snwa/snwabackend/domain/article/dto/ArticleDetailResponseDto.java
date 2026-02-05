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
    private boolean isLiked;
    private Long likeCount;
    private Long clickCount;
    /** 해당 기사에 유저가 코인을 사용했는지 여부 (admin이면 true) */
    private boolean hasUsedCoin;

    public static ArticleDetailResponseDto from(Article article) {
        return from(article, false, false);
    }

    public static ArticleDetailResponseDto from(Article article, boolean isBookmarked) {
        return from(article, isBookmarked, false);
    }

    public static ArticleDetailResponseDto from(Article article, boolean isBookmarked, boolean isLiked) {
        return from(article, isBookmarked, isLiked, 0L, article.getClickCount(), false);
    }

    public static ArticleDetailResponseDto from(Article article, boolean isBookmarked, boolean isLiked, Long likeCount, Long displayedClickCount) {
        return from(article, isBookmarked, isLiked, likeCount, displayedClickCount, false);
    }

    public static ArticleDetailResponseDto from(Article article, boolean isBookmarked, boolean isLiked, Long likeCount, Long displayedClickCount, boolean hasUsedCoin) {
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
                .isLiked(isLiked)
                .likeCount(likeCount != null ? likeCount : 0L)
                .clickCount(displayedClickCount != null ? displayedClickCount : 0L)
                .hasUsedCoin(hasUsedCoin)
                .build();
    }
}
