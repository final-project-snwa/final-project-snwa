package com.team.snwa.snwabackend.domain.crawler.repository;

import com.team.snwa.snwabackend.domain.crawler.entity.ArticleCrawlingTracking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleCrawlingTrackingRepository extends JpaRepository<ArticleCrawlingTracking, Long> {
    void deleteByJobId(Long jobId);
}
