package com.team.snwa.snwabackend.domain.article.dto;

import com.team.snwa.snwabackend.domain.article.dto.response.ReactionCountResponseDto;
import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.entity.enums.ReactionType;
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
    
    // 감정 반응 관련 필드 (좋아요 포함)
    private Long likeCount;
    private Long dislikeCount;
    private Long sadCount;
    private Long angryCount;
    private ReactionType userReaction;

    /** 해당 기사에 유저가 코인을 사용했는지 여부 (admin이면 true) */
    private boolean hasUsedCoin;

    public static ArticleDetailResponseDto from(Article article) {
        return from(article, false, article.getClickCount(), null, false);
    }

    public static ArticleDetailResponseDto from(Article article, boolean isBookmarked, Long displayedClickCount, ReactionCountResponseDto reactionCounts) {
        return from(article, isBookmarked, displayedClickCount, reactionCounts, false);
    }

    public static ArticleDetailResponseDto from(Article article, boolean isBookmarked, Long displayedClickCount, ReactionCountResponseDto reactionCounts, boolean hasUsedCoin) {
        ArticleDetailResponseDtoBuilder builder = ArticleDetailResponseDto.builder()
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
                .hasUsedCoin(hasUsedCoin);

        // 반응 정보 추가
        if (reactionCounts != null) {
            builder.likeCount(reactionCounts.getLikeCount())
                    .dislikeCount(reactionCounts.getDislikeCount())
                    .sadCount(reactionCounts.getSadCount())
                    .angryCount(reactionCounts.getAngryCount())
                    .userReaction(reactionCounts.getUserReaction());
        } else {
            builder.likeCount(0L)
                    .dislikeCount(0L)
                    .sadCount(0L)
                    .angryCount(0L)
                    .userReaction(null);
        }

        return builder.build();
    }
}
