package com.team.snwa.snwabackend.domain.notification.dto.response;

/**
 * 알림 설정 상태
 * @param enableNotification 알림 수신 on/off
 */
public record NotificationSettingResponse(boolean enableNotification) {}