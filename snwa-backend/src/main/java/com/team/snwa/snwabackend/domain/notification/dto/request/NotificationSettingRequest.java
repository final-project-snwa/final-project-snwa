package com.team.snwa.snwabackend.domain.notification.dto.request;

/**
 * 알림 설정 변경 요청
 * @param enableNotification 변경할 알림 수신 상태
 */
public record NotificationSettingRequest(boolean enableNotification) {}