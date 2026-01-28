package com.team.snwa.snwabackend.domain.article.dto.response;

import java.time.LocalDateTime;

public record AdminArticleListResponse(
    Long id,
    String title,
    String userNickname,  // 등록한 사용자의 닉네임
    LocalDateTime createdDate  // 등록 날짜
) {
}
