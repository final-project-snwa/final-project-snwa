package com.team.snwa.snwabackend.domain.comment.entity;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Comment(String content, Article article, User user) {
        this.content = content;
        this.article = article;
        this.user = user;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}