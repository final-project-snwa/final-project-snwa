package com.team.snwa.snwabackend.domain.crawler.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SystemStatusDto {

    private String totalMemory;
    private String freeMemory;
    private String usedMemory;

    private int availableProcessors;
    private int activeSystemThreads;

    private int runningCrawlerCount;
    private List<Long> runningJobIds;
}
