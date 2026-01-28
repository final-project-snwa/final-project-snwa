package com.team.snwa.snwabackend.domain.crawler.service;

import com.team.snwa.snwabackend.domain.crawler.dto.SystemStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 서버의 시스템 리소스 및 크롤러 가동 현황을 모니터링하는 서비스
 *
 * @author 허준형
 * @DateOfCreated 2026-01-28
 * @DateOfEdit 2026-01-28
 */
@Service
@RequiredArgsConstructor
public class SystemMonitorService {

    private final DynamicSchedulingService schedulingService;

    /**
     * 현재 서버의 시스템 상태와 크롤러 실행 정보를 종합하여 DTO로 반환함
     *
     * @author 허준형
     * @DateOfCreated 2026-01-28
     * @DateOfEdit 2026-01-28
     */
    public SystemStatusDto getCurrentSystemStatus() {

        Runtime runtime = Runtime.getRuntime();

        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;

        List<Long> runningIds = schedulingService.getRunningJobIds();

        return SystemStatusDto.builder()
                .totalMemory(formatMb(total))
                .freeMemory(formatMb(free))
                .usedMemory(formatMb(used))
                .availableProcessors(runtime.availableProcessors())
                .activeSystemThreads(Thread.activeCount())
                .runningCrawlerCount(runningIds.size())
                .runningJobIds(runningIds)
                .build();
    }

    private String formatMb(long bytes) {
        long mb = bytes / (1024 * 1024);
        return mb + "MB";
    }
}
