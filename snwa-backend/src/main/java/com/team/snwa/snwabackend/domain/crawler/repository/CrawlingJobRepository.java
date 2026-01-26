package com.team.snwa.snwabackend.domain.crawler.repository;

import com.team.snwa.snwabackend.domain.crawler.entity.CrawlingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrawlingJobRepository extends JpaRepository<CrawlingJob, Long> {
    List<CrawlingJob> findByIsActiveTrue();
}
