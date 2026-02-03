package com.team.snwa.snwabackend.domain.notification.entity;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_user", columnList = "user_id"),
                @Index(name = "idx_notification_user_read", columnList = "user_id, is_read")
        }
)
@Getter

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //알림 대상
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //관련 기사
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    //알림 내용
    @Column(nullable = false, length = 255)
    private String message;

    //읽음 여뷰
    @Column(name = "is_read", nullable = false)
    private boolean isRead;


    private Notification( User user, Article article, String message) {
        this.user = user;
        this.article = article;
        this.message = message;
        this.isRead = false;
    }

    public static Notification create(User user, Article article, String message) {
        return new Notification(user,article,message);
    }

    public void markAsRead() {
        this.isRead = true;
    }
}