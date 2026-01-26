package com.team.snwa.snwabackend.domain.crawler.repository;

import com.team.snwa.snwabackend.domain.crawler.entity.CrawlingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlingLogRepository extends JpaRepository<CrawlingLog, Long> {
}
