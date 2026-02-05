package com.team.snwa.snwabackend.domain.article.dto.response;

import com.team.snwa.snwabackend.domain.article.entity.enums.ReactionType;
import lombok.Builder;
import lombok.Getter;

/**
 * 기사의 반응 개수 응답 DTO
 */
@Getter
@Builder
public class ReactionCountResponseDto {
    private long likeCount;
    private long dislikeCount;
    private long sadCount;
    private long angryCount;
    private ReactionType userReaction;  // 현재 사용자의 반응 (없으면 null)

    public static ReactionCountResponseDto of(long likeCount, long dislikeCount, long sadCount, long angryCount, ReactionType userReaction) {
        return ReactionCountResponseDto.builder()
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .sadCount(sadCount)
                .angryCount(angryCount)
                .userReaction(userReaction)
                .build();
    }
}
