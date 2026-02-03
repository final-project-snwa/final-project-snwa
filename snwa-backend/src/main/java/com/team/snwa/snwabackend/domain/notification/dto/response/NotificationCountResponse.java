package com.team.snwa.snwabackend.domain.notification.dto.response;

/**
 * 읽지 않은 알림 개수 반환
 * @param unreadCount
 */
public record NotificationCountResponse(long unreadCount) {}