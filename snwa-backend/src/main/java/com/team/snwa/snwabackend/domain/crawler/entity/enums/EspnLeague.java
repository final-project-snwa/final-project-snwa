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

    NBA("NBA", "basketball/nba", "https://www.skysports.com/nba/news"),

    EPL("EPL (잉글랜드)", "soccer/eng.1", "https://www.skysports.com/premier-league-news"),
    LALIGA("La Liga (스페인)", "soccer/esp.1", "https://www.skysports.com/la-liga-news"),
    BUNDESLIGA("Bundesliga (독일)", "soccer/ger.1", "https://www.skysports.com/bundesliga-news"),
    CHAMPIONS_LEAGUE("UCL (챔피언스리그)", "soccer/uefa.champions", "https://www.skysports.com/football/competitions/champions-league/news"),

    MLB("MLB", "baseball/mlb", "https://www.skysports.com/mlb/news");

    private final String description;
    private final String apiPath;       // ESPN용 API 경로
    private final String skySportsUrl;  // Sky Sports용 URL
}