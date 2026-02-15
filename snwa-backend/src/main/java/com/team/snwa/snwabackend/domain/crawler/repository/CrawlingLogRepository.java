package com.team.snwa.snwabackend.domain.crawler.repository;

import com.team.snwa.snwabackend.domain.crawler.entity.CrawlingLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrawlingLogRepository extends JpaRepository<CrawlingLog, Long> {
    /**
     * 문제 : 관리자 페이지 -> 크롤러 관리 -> 크롤링 주기가 짧을 때 JOB삭제 시 충돌이 있었음.
     * 크롤링 주기가 짧을 때(10초 등) Job 삭제 시 발생하는 경쟁 상태(Race Condition) 해결
     * 기존 JPA 삭제는 건별로 진행되어 삭제 도중 새로운 로그가 쌓이면 제약조건 위배(Constraint Violation) 발생함.
     * 이를 방지하기 위해 벌크 연산(@Modifying, @Query)으로 해당 Job의 모든 로그를 한 번에 즉시 삭제함.
     *
     */
    @Modifying
    @Query("DELETE FROM CrawlingLog l WHERE l.crawlingJob.id = :jobId")
    void deleteByCrawlingJobId(@Param("jobId") Long jobId);

    Page<CrawlingLog> findByCrawlingJobId(Long jobId, Pageable pageable);
}
