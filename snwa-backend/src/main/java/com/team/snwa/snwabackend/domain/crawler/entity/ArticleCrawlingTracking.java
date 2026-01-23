package com.team.snwa.snwabackend.domain.crawler.entity;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private String titleOrigin; // 수집된 원문 제목 (영어 등)

    @Column(name = "content_origin", columnDefinition = "TEXT")
    private String contentOrigin; // 수집된 원문 본문 (영어 등)

    @Column(name = "title_ko")
    private String titleKo; // AI가 번역한 결과물 제목

    @Column(name = "content_ko", columnDefinition = "TEXT")
    private String contentKo; // AI가 번역한 결과물 본문
}