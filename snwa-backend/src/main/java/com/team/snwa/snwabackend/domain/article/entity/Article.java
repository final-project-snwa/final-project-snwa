package com.team.snwa.snwabackend.domain.article.entity;

import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "articles")
@Getter
public class Article extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String title;

    private String translatedTitle;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Setter
    private String translatedTitle;

    @Column(columnDefinition = "LONGTEXT")
    @Setter
    private String translatedContent;

    private String summary;

    @Column(unique = true)
    private String originalUrl;

    private String authorName;
    private String publisherName;
}