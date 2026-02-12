package com.team.snwa.snwabackend.domain.user.dto.response;

import com.team.snwa.snwabackend.domain.exp.dto.ExpGrantInfoDto;

/**
 * 로그인 성공 시 반환되는 결과 DTO
 */
public record LoginResult(
        String token,
        boolean attendanceRewardGiven,
        ExpGrantInfoDto expGrantInfo
) {
}
