package com.team.snwa.snwabackend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 동적 스케줄링을 위한 스레드 풀 설정 클래스
 * 크롤링 작업이 늘어날 것을 대비해 Pool Size를 넉넉히 설정함
 *
 * @author 허준형
 * @DateOfCreated 2026-01-27
 */
@Configuration
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(10); // 동시에 실행될 수 있는 크롤링 작업 최대 개수 TODO : 서버 사양에 따라 조절 해야함
        scheduler.setThreadNamePrefix("Crawling-Scheduler");
        scheduler.initialize();


        return scheduler;
    }
}
