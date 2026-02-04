package com.team.snwa.snwabackend.domain.user.dto.response;

import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.entity.enums.UserRole;
import com.team.snwa.snwabackend.domain.user.entity.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

public record UserProfileResponse(
        Long id,
        String email,
        String nickname,
        String introduction,
        String phoneNumber,
        String profileImageUrl,
        UserStatus status,
        UserRole role,
        LocalDateTime createdDate,
        LocalDateTime updatedDate,
        List<CategoryClickCountDto> categoryClickCount
        ) {
    public static UserProfileResponse from(User user, List<CategoryClickCountDto> categoryClickCount) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getIntroduction(),
                user.getPhoneNumber(),
                user.getProfileImageUrl(),
                user.getStatus(),
                user.getRole(),
                user.getCreatedDate(),
                user.getUpdatedDate(),
                categoryClickCount != null ? categoryClickCount : List.of());
    }
}
