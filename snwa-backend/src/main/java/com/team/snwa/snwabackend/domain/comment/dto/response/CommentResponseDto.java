package com.team.snwa.snwabackend.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.snwa.snwabackend.domain.comment.entity.Comment;
import com.team.snwa.snwabackend.domain.exp.dto.ExpGrantInfoDto;
import com.team.snwa.snwabackend.domain.user.entity.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponseDto {

    private Long commentId;
    private String content;
    private Long userId;
    private String nickname;
    private Integer authorLevel;
    private String profileImageUrl;
    private boolean isAdmin;
    @JsonProperty("isMine")
    private boolean isMine;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ExpGrantInfoDto expGrantInfo;

    public static CommentResponseDto from(Comment comment) {
        return from(comment, null);
    }

    public static CommentResponseDto from(Comment comment, Long currentUserId) {
        return from(comment, currentUserId, null);
    }

    public static CommentResponseDto from(Comment comment, Long currentUserId, Integer authorLevel) {
        boolean isAdmin = comment.getUser().getRole() == UserRole.ADMIN;
        boolean isMine = currentUserId != null && comment.getUser().getId().equals(currentUserId);
        return CommentResponseDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .nickname(comment.getUser().getNickname())
                .authorLevel(authorLevel)
                .profileImageUrl(comment.getUser().getProfileImageUrl())
                .isAdmin(isAdmin)
                .isMine(isMine)
                .createdAt(comment.getCreatedDate())
                .updatedAt(comment.getUpdatedDate())
                .build();
    }
}
