package com.team.snwa.snwabackend.domain.crawler.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CrawlingJobUpdateDto {
    private String cronExpression;
    private Boolean isActive;
}
