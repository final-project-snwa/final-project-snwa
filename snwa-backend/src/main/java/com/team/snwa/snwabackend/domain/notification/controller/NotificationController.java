package com.team.snwa.snwabackend.domain.notification.controller;

import com.team.snwa.snwabackend.domain.notification.dto.request.NotificationSettingRequest;
import com.team.snwa.snwabackend.domain.notification.dto.response.NotificationActionResponse;
import com.team.snwa.snwabackend.domain.notification.dto.response.NotificationCountResponse;
import com.team.snwa.snwabackend.domain.notification.dto.response.NotificationResponse;
import com.team.snwa.snwabackend.domain.notification.dto.response.NotificationSettingResponse;
import com.team.snwa.snwabackend.domain.notification.service.NotificationService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    /**
     * 알림 목록 조회 (전체)
     */
    @GetMapping
    public Page<NotificationResponse> getNotifications(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        return notificationService.getMyNotifications(user, pageable)
                .map(NotificationResponse::from);
    }

    /**
     * 읽지 않은 알림 목록 조회
     */
    @GetMapping("/unread")
    public Page<NotificationResponse> getUnreadNotifications(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        return notificationService.getUnreadNotifications(user, pageable)
                .map(NotificationResponse::from);
    }

    /**
     * 읽지 않은 알림 개수 조회 (뱃지용)
     */
    @GetMapping("/unread/count")
    public NotificationCountResponse getUnreadCount(
            @AuthenticationPrincipal User user
    ) {
        long count = notificationService.getUnreadCount(user);
        return new NotificationCountResponse(count);
    }

    /**
     * 알림 단건 읽음 처리
     */
    @PatchMapping("/{notificationId}/read")
    public NotificationActionResponse readNotification(
            @AuthenticationPrincipal User user,
            @PathVariable Long notificationId
    ) {
        notificationService.readNotification(notificationId, user);
        return new NotificationActionResponse(true);
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PatchMapping("/read-all")
    public NotificationActionResponse readAllNotifications(
            @AuthenticationPrincipal User user
    ) {
        notificationService.readAll(user);
        return new NotificationActionResponse(true);
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    public NotificationActionResponse deleteNotification(
            @AuthenticationPrincipal User user,
            @PathVariable Long notificationId
    ) {
        notificationService.delete(notificationId, user);
        return new NotificationActionResponse(true);
    }

    /**
     * 알림 설정 조회
     */
    @GetMapping("/settings")
    public NotificationSettingResponse getNotificationSetting(
            @AuthenticationPrincipal User user
    ) {
        return notificationService.getSetting(user);
    }

    /**
     * 알림 설정 변경
     */
    @PatchMapping("/settings")
    public NotificationSettingResponse updateNotificationSetting(
            @AuthenticationPrincipal User user,
            @RequestBody NotificationSettingRequest request
    ) {
        return notificationService.updateSetting(user, request);
    }

}
