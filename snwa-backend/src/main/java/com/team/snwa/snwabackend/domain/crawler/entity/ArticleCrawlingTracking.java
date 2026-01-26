package com.team.snwa.snwabackend.domain.crawler.entity;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


/**
 * 크롤링된 기사의 추적 정보를 저장하는 엔티티
 * 실제 Article 엔티티와 1:1로 매핑되며, 수집 당시의 원문 데이터(제목, 본문)와 번역 상태, 출처 Job 정보를 관리함
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2026-01-26
 */
@Entity
@Table(name = "article_crawling_tracking") // 기사 크롤링 추적
@Getter
@Setter
public class ArticleCrawlingTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article; // 실제 기사 테이블과 1:1 연결

    private Long jobId; // 수집 출처 Job ID (FK)

    private Long categoryId; // 기사 종목

    @Column(name = "article_url")
    private String articleUrl; // 원문 출처 링크

    @Column(name = "title_origin")
    private String titleOrigin; // 수집된 원문 제목

    @Column(name = "content_origin", columnDefinition = "TEXT")
    private String contentOrigin; // 수집된 원문 본문

    @Column(name = "title_ko")
    private String titleKo; // AI가 번역한 결과물 제목

    @Column(name = "content_ko", columnDefinition = "TEXT")
    private String contentKo; // AI가 번역한 결과물 본문
}