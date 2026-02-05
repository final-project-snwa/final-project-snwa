package com.team.snwa.snwabackend.domain.notification.event;

import com.team.snwa.snwabackend.domain.interest.entity.InterestType;

import java.util.Map;
//해당 기사가 번역+요약+키워드 추출까지 끝났는지를 알려주는 이벤트
public record ArticleReadyForNotificationEvent(
        Long articleId,
        Map<String, InterestType> typedKeywords
) {}