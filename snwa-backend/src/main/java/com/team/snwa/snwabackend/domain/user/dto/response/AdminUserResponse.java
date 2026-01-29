package com.team.snwa.snwabackend.domain.user.dto.response;

import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.entity.enums.UserRole;
import com.team.snwa.snwabackend.domain.user.entity.enums.UserStatus;

import java.time.LocalDateTime;

public record AdminUserResponse(
    Long id,
    String email,
    String nickname,
    String profileImageUrl,
    UserStatus status,
    UserRole role,
    Boolean emailVerified,
    String introduction,
    String phoneNumber,
    LocalDateTime createdDate,
    LocalDateTime updatedDate,
    LocalDateTime deletedAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImageUrl(),
            user.getStatus(),
            user.getRole(),
            user.getEmailVerified(),
            user.getIntroduction(),
            user.getPhoneNumber(),
            user.getCreatedDate(),
            user.getUpdatedDate(),
            user.getDeletedAt()
        );
    }
}
