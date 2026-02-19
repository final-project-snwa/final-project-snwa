package com.team.snwa.snwabackend.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 사용자 프로필 수정 요청 DTO
 * 닉네임, 소개유, 전화번호, 프로필 이미지 URL, 디스코드 웹후크 URL 포함
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2026-02-19
 */
public record UserProfileUpdateRequest(
        @NotBlank(message = "닉네임은 필수입니다.") @Size(max = 12, message = "닉네임은 12자 이하여야 합니다") String nickname,

        @Size(max = 255, message = "소개는 255자 이내여야 합니다.") String introduction,

        @Size(max = 20, message = "연락처는 20자 이내여야 합니다.") @Pattern(regexp = "^(|01[016789][-]?\\d{3,4}[-]?\\d{4}|02[-]?\\d{3,4}[-]?\\d{4}|0[3-6][1-5][-]?\\d{3,4}[-]?\\d{4})$", message = "전화번호 형식이 올바르지 않습니다.") String phoneNumber,

        String profileImageUrl,
        
        @Size(max = 255, message = "디스코드 웹후크 URL은 255자 이내여야 합니다.")
        @Pattern(regexp = "^(https://discord.com/api/webhooks/.*)?$", message = "올바른 디스코드 웹후크 URL 형식이 아닙니다.")
        String discordWebhookUrl) {
}
