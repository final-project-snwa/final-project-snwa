package com.team.snwa.snwabackend.domain.translation.entity;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "translation_access_logs", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "article_id", "language" })
})
public class TranslationAccessLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(nullable = false)
    private String language;

    @Builder
    public TranslationAccessLog(User user, Article article, String language) {
        this.user = user;
        this.article = article;
        this.language = language;
    }
}
