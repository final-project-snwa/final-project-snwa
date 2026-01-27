package com.team.snwa.snwabackend.domain.crawler.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ESPN API에서 사용하는 종목 및 리그 코드를 관리하는 Enum
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2026-01-26
 */
@Getter
@RequiredArgsConstructor
public enum EspnLeague {

    NBA("NBA", "basketball/nba"),

    EPL("EPL (잉글랜드)", "soccer/eng.1"),
    LALIGA("La Liga (스페인)", "soccer/esp.1"),
    BUNDESLIGA("Bundesliga (독일)", "soccer/ger.1"),
    CHAMPIONS_LEAGUE("UCL (챔피언스리그)", "soccer/uefa.champions"),

    MLB("MLB", "baseball/mlb");

    private final String description;
    private final String apiPath;
}