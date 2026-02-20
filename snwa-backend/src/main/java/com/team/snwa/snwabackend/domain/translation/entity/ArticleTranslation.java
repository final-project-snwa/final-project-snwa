package com.team.snwa.snwabackend.domain.translation.entity;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "article_translations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"article_id", "language"})
})
public class ArticleTranslation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(nullable = false, length = 10)
    private String language; // e.g., "KO", "JA", "EN"

    private String translatedTitle;

    @Column(columnDefinition = "LONGTEXT")
    private String translatedContent;

    @Column(columnDefinition = "LONGTEXT")
    private String summary;

    @Builder
    public ArticleTranslation(Article article, String language, String translatedTitle, String translatedContent, String summary) {
        this.article = article;
        this.language = language;
        this.translatedTitle = translatedTitle;
        this.translatedContent = translatedContent;
        this.summary = summary;
    }

    public void updateSummary(String summary) {
        this.summary = summary;
    }
}