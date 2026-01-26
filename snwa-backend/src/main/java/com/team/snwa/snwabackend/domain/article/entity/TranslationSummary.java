package com.team.snwa.snwabackend.domain.article.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "translation_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TranslationSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 번역 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article; // 대상 기사 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // 카테고리 ID

    @Column(columnDefinition = "TEXT")
    private String text; // 실제 번역/요약문 내용

    @Builder
    public TranslationSummary(Article article, Category category, String text) {
        this.article = article;
        this.category = category;
        this.text = text;
    }
}