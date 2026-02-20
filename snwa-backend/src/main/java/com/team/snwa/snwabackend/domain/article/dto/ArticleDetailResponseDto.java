package com.team.snwa.snwabackend.domain.article.dto;

import com.team.snwa.snwabackend.domain.article.dto.response.ReactionCountResponseDto;
import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.entity.enums.ReactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

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

    /** 이 기사에서 구매한 번역 언어 코드 목록 (KO, JA, EN, ZH 등) - 이미 구매한 언어 재열람 시 확인창 생략용 */
    private List<String> purchasedTranslationLanguages;

    public static ArticleDetailResponseDto from(Article article) {
        return from(article, null, false, article.getClickCount(), null, false, null);
    }

    public static ArticleDetailResponseDto from(Article article,
            com.team.snwa.snwabackend.domain.translation.entity.ArticleTranslation translation, boolean isBookmarked,
            Long displayedClickCount, ReactionCountResponseDto reactionCounts) {
        return from(article, translation, isBookmarked, displayedClickCount, reactionCounts, false, null);
    }

    public static ArticleDetailResponseDto from(Article article,
            com.team.snwa.snwabackend.domain.translation.entity.ArticleTranslation translation, boolean isBookmarked,
            Long displayedClickCount, ReactionCountResponseDto reactionCounts, boolean hasUsedCoin) {
        return from(article, translation, isBookmarked, displayedClickCount, reactionCounts, hasUsedCoin, null);
    }

    public static ArticleDetailResponseDto from(Article article,
            com.team.snwa.snwabackend.domain.translation.entity.ArticleTranslation translation, boolean isBookmarked,
            Long displayedClickCount, ReactionCountResponseDto reactionCounts, boolean hasUsedCoin,
            List<String> purchasedTranslationLanguages) {
        ArticleDetailResponseDtoBuilder builder = ArticleDetailResponseDto.builder()
                .id(article.getId())
                .title(article.getTitle()) // 원문 제목
                .translatedTitle(translation != null ? translation.getTranslatedTitle() : article.getTitle()) // 번역 없으면
                                                                                                              // 원문
                .content(article.getContent()) // 원문
                .translatedContent(translation != null ? translation.getTranslatedContent() : article.getContent()) // 번역
                                                                                                                    // 없으면
                                                                                                                    // 원문
                .summary(translation != null ? translation.getSummary() : null) // 번역 없으면 없음
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
                .hasUsedCoin(hasUsedCoin)
                .purchasedTranslationLanguages(
                        purchasedTranslationLanguages != null ? purchasedTranslationLanguages : List.of());

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
