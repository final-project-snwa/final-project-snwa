package com.team.snwa.snwabackend.domain.notification.controller;

import com.team.snwa.snwabackend.domain.notification.dto.request.NotificationSettingRequest;
import com.team.snwa.snwabackend.domain.notification.dto.response.NotificationActionResponse;
import com.team.snwa.snwabackend.domain.notification.dto.response.NotificationCountResponse;
import com.team.snwa.snwabackend.domain.notification.dto.response.NotificationResponse;
import com.team.snwa.snwabackend.domain.notification.dto.response.NotificationSettingResponse;
import com.team.snwa.snwabackend.domain.notification.service.NotificationService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;


    /**
     * 알림 목록 조회 (전체)
     */
    @GetMapping
    public Page<NotificationResponse> getNotifications(
            Principal principal,
            Pageable pageable
    ) {
        User user = resolveUser(principal);
        if (user == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return notificationService.getMyNotifications(user, pageable)
                .map(NotificationResponse::from);
    }

    /**
     * 읽지 않은 알림 목록 조회
     */
    @GetMapping("/unread")
    public Page<NotificationResponse> getUnreadNotifications(
            Principal principal,
            Pageable pageable
    ) {
        User user = resolveUser(principal);
        if (user == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return notificationService.getUnreadNotifications(user, pageable)
                .map(NotificationResponse::from);
    }

    /**
     * 읽지 않은 알림 개수 조회 (뱃지용)
     */
    @GetMapping("/unread/count")
    public NotificationCountResponse getUnreadCount(
            Principal principal
    ) {
        User user = resolveUser(principal);
        if (user == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        long count = notificationService.getUnreadCount(user);
        return new NotificationCountResponse(count);
    }

    /**
     * 알림 단건 읽음 처리
     */
    @PatchMapping("/{notificationId}/read")
    public NotificationActionResponse readNotification(
            Principal principal,
            @PathVariable Long notificationId
    ) {
        User user = resolveUser(principal);
        if (user == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        notificationService.readNotification(notificationId, user);
        return new NotificationActionResponse(true);
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PatchMapping("/read-all")
    public NotificationActionResponse readAllNotifications(
            Principal principal
    ) {
        User user = resolveUser(principal);
        if (user == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        notificationService.readAll(user);
        return new NotificationActionResponse(true);
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    public NotificationActionResponse deleteNotification(
            Principal principal,
            @PathVariable Long notificationId
    ) {
        User user = resolveUser(principal);
        if (user == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        notificationService.delete(notificationId, user);
        return new NotificationActionResponse(true);
    }

    /**
     * 알림 설정 조회
     */
    @GetMapping("/settings")
    public NotificationSettingResponse getNotificationSetting(
            Principal principal
    ) {
        User user = resolveUser(principal);
        if (user == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return notificationService.getSetting(user);
    }

    /**
     * 알림 설정 변경
     */
    @PatchMapping("/settings")
    public NotificationSettingResponse updateNotificationSetting(
            Principal principal,
            @RequestBody NotificationSettingRequest request
    ) {
        User user = resolveUser(principal);
        if (user == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return notificationService.updateSetting(user, request);
    }

    private User resolveUser(Principal principal) {
        if (principal == null || principal.getName() == null) return null;
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }
}
