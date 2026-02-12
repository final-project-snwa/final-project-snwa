package com.team.snwa.snwabackend.domain.exp.dto;

public record LeaderboardEntryDto(
        int rank,
        Long userId,
        String nickname,
        int level,
        long totalExp
) {}
