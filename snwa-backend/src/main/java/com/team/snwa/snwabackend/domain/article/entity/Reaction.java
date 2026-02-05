package com.team.snwa.snwabackend.domain.article.entity;

import com.team.snwa.snwabackend.domain.article.entity.enums.ReactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 기사에 대한 감정 반응 엔티티
 * 한 사용자는 한 기사에 하나의 반응만 가능
 */
@Entity
@Table(name = "article_reactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "article_id"}))
@Getter
@NoArgsConstructor
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    private ReactionType reactionType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Reaction(Long userId, Long articleId, ReactionType reactionType) {
        this.userId = userId;
        this.articleId = articleId;
        this.reactionType = reactionType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 반응 타입 변경
     */
    public void changeReactionType(ReactionType newType) {
        this.reactionType = newType;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
