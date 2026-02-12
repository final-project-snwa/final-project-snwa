package com.team.snwa.snwabackend.domain.exp.dto;

public record UserExpSummaryDto(
        int level,
        long totalExp,
        long expToNextLevel,
        long expProgress
) {}
