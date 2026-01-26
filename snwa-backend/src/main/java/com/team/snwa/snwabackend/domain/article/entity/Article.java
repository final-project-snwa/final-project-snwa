package com.team.snwa.snwabackend.domain.article.entity;

import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
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

    private String imageUrl;

    @Builder
    public Article(Category category, String title, String content, String translatedContent,
                    String summary, String originalUrl, String authorName, String publisherName,
                    String imageUrl) { // ✅ 생성자에도 추가
        this.category = category;
        this.title = title;
        this.content = content;
        this.translatedContent = translatedContent;
        this.summary = summary;
        this.originalUrl = originalUrl;
        this.authorName = authorName;
        this.publisherName = publisherName;
        this.imageUrl = imageUrl; // ✅ 할당
    }

    public Article() {}
}