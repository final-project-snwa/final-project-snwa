package com.team.snwa.snwabackend.domain.user.dto.response;

public record ProfileImagePresignedUrlResponse(
        String presignedUrl,
        String fileUrl) {
}
