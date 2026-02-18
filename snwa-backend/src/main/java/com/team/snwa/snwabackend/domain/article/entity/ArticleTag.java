package com.team.snwa.snwabackend.domain.article.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(name = "tag_name")
    private String tagName;

    @Column(length = 10)
    private String language;

    @Builder
    public ArticleTag(Article article, String tagName, String language) {
        this.article = article;
        this.tagName = tagName;
        this.language = language;
    }
}