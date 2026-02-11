package com.team.snwa.snwabackend.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private String message;
    /** 당일 첫 로그인 시 출석 코인 지급 여부 (true면 1코인 지급됨, 이때만 프론트에서 안내 모달 표시) */
    private Boolean attendanceRewardGiven;
}
