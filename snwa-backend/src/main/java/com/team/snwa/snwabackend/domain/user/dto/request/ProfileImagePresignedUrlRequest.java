package com.team.snwa.snwabackend.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProfileImagePresignedUrlRequest(
        @NotBlank(message = "파일 타입은 필수입니다.") String contentType) {
}
