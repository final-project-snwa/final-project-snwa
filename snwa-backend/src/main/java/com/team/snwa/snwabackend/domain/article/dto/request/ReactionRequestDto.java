package com.team.snwa.snwabackend.domain.article.dto.request;

import com.team.snwa.snwabackend.domain.article.entity.enums.ReactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 반응 생성/수정 요청 DTO
 */
@Getter
@NoArgsConstructor
public class ReactionRequestDto {
    
    @NotNull(message = "반응 타입은 필수입니다.")
    private ReactionType reactionType;
}
