package com.team.snwa.snwabackend.domain.user.dto.response;

import com.team.snwa.snwabackend.domain.comment.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminUserCommentResponse {
    private final Long commentId;
    private final String content;
    private final Long articleId;
    private final String articleTitle;
    private final LocalDateTime createdAt;

    public AdminUserCommentResponse(Long commentId, String content, Long articleId, String articleTitle,
            LocalDateTime createdAt) {
        this.commentId = commentId;
        this.content = content;
        this.articleId = articleId;
        this.articleTitle = articleTitle;
        this.createdAt = createdAt;
    }

    public static AdminUserCommentResponse from(Comment comment) {
        String title = comment.getArticle().getTitle();
        return new AdminUserCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getArticle().getId(),
                title,
                comment.getCreatedDate());
    }
}
