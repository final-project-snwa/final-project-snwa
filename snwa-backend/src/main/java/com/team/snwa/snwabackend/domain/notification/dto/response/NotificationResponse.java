package com.team.snwa.snwabackend.domain.notification.dto.response;

import com.team.snwa.snwabackend.domain.notification.entity.Notification;

import java.time.LocalDateTime;

/**
 * 알람 한 건
 * @param notificationId 알림 식별자
 * @param message 알림 메시지
 * @param isRead 읽음 여부
 * @param createdDate 알림 생성 시각
 * @param articleId 연결된 기사 ID
 * @param articleTitle 기사 제목
 */
public record NotificationResponse(
        Long notificationId,
        String message,
        boolean isRead,
        LocalDateTime createdDate,
        Long articleId,
        String articleTitle
) {
    public static NotificationResponse from(Notification notification) {
        String articleTitle = null;
        if (notification.getArticle() != null) {
            String translatedTitle = notification.getArticle().getTranslatedTitle();
            if (translatedTitle != null && !translatedTitle.isBlank()) {
                articleTitle = translatedTitle;
            } else {
                articleTitle = notification.getArticle().getTitle();
            }
        }
        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedDate(),
                notification.getArticle() != null ? notification.getArticle().getId() : null,
                articleTitle
        );
    }
}
