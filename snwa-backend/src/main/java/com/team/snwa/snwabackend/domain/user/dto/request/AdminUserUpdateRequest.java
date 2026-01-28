package com.team.snwa.snwabackend.domain.user.dto.request;

import com.team.snwa.snwabackend.domain.user.entity.enums.UserStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminUserUpdateRequest(
    @Size(max = 12, message = "닉네임은 12자 이하여야 합니다") String nickname,
    @Size(max = 255, message = "소개는 255자 이내여야 합니다.") String introduction,
    @Size(max = 20, message = "연락처는 20자 이내여야 합니다.") 
    @Pattern(regexp = "^(|01[016789][-]?\\d{3,4}[-]?\\d{4}|02[-]?\\d{3,4}[-]?\\d{4}|0[3-6][1-5][-]?\\d{3,4}[-]?\\d{4})$", 
             message = "전화번호 형식이 올바르지 않습니다.") String phoneNumber,
    String profileImageUrl,
    UserStatus status,      // 관리자만 상태 변경 가능
    Boolean emailVerified   // 관리자만 이메일 인증 상태 변경 가능
) {
}
