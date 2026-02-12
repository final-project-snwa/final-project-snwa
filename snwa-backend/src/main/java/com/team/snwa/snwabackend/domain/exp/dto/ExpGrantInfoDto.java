package com.team.snwa.snwabackend.domain.exp.dto;

public record ExpGrantInfoDto(
        int expGained,
        boolean levelUp,
        int newLevel
) {}
