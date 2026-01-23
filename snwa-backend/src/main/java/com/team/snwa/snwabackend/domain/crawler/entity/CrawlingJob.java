package com.team.snwa.snwabackend.domain.crawler.entity;

import com.team.snwa.snwabackend.domain.article.entity.Category;
import com.team.snwa.snwabackend.domain.crawler.entity.enums.SourceName;
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "crawling_job")
@Getter
public class CrawlingJob extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // 종목 분류

    @Enumerated(EnumType.STRING)
    private SourceName sourceName; // ESPN, BBC 등

    private String targetUrl; // 타겟 URL
    private String cronExpression; // 수집 주기

    @Column(columnDefinition = "TINYINT(1)")
    private boolean isActive; // 활성 여부

    private LocalDateTime lastRunAt; // 마지막 실행일
}