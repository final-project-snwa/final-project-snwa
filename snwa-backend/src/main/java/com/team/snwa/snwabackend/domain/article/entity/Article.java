package com.team.snwa.snwabackend.domain.article.entity;

import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // 글을 등록한 사용자

    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Setter
    private String translatedTitle;

    @Column(columnDefinition = "LONGTEXT")
    @Setter
    private String translatedContent;

    @Column(columnDefinition = "LONGTEXT")
    @Setter
    private String summary;

    @Column(unique = true)
    private String originalUrl;

    private String authorName;
    private String publisherName;

    private String imageUrl;

    // 소프트 삭제를 위한 필드
    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @Builder
    public Article(Category category, User user, String title, String content, String translatedContent,
                    String summary, String originalUrl, String authorName, String publisherName,
                    String imageUrl) {
        this.category = category;
        this.user = user;
        this.title = title;
        this.content = content;
        this.translatedContent = translatedContent;
        this.summary = summary;
        this.originalUrl = originalUrl;
        this.authorName = authorName;
        this.publisherName = publisherName;
        this.imageUrl = imageUrl;
    }

    public Article() {}

    /**
     * 소프트 삭제 메서드
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}