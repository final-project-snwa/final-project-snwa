package com.team.snwa.snwabackend.domain.notification.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.notification.dto.request.NotificationSettingRequest;
import com.team.snwa.snwabackend.domain.notification.dto.response.NotificationResponse;
import com.team.snwa.snwabackend.domain.notification.dto.response.NotificationSettingResponse;
import com.team.snwa.snwabackend.domain.notification.entity.Notification;
import com.team.snwa.snwabackend.domain.notification.entity.NotificationSetting;
import com.team.snwa.snwabackend.domain.notification.repository.NotificationRepository;
import com.team.snwa.snwabackend.domain.notification.repository.NotificationSettingRepository;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final SseEmitterService sseEmitterService;
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    /**
     * 알림 생성 (AI 키워드 추출 시 호출)
     * 유저의 알림 설정이 활성화되어 있을 때만 알림 생성
     */
    @Transactional
    public void createNotification(User user, Article article, String message) {
        notificationSettingRepository.findByUser(user).ifPresentOrElse(
                setting -> {
                    if (setting.isEnableNotification()) {
                        Notification notification = Notification.create(user, article, message);
                        notificationRepository.save(notification);
                    }
                },
                () -> {
                    // 알림 설정이 없는 경우에도 알림 생성 (기본값: 알림 허용)
                    Notification notification = Notification.create(user, article, message);
                    notificationRepository.save(notification);

                    //실시간 알림 push
                    sseEmitterService.send(
                            user.getId(),
                            NotificationResponse.from(notification)
                    );
                });
    }

    // 알림 목록 조회
    @Transactional(readOnly = true)
    public Page<Notification> getMyNotifications(User user, Pageable pageable) {
        validateNotificationEnabled(user);
        return notificationRepository
                .findByUserOrderByCreatedDateDesc(user, pageable);
    }

    // 읽지 않은 알림 목록
    public Page<Notification> getUnreadNotifications(User user, Pageable pageable) {
        validateNotificationEnabled(user);
        return notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedDateDesc(user, pageable);
    }

    // 알림 읽음 처리
    @Transactional
    public void readNotification(Long notificationId, User user) {
        validateNotificationEnabled(user);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        validateOwner(notification, user);

        notification.markAsRead();
    }

    // 모든 알림 읽음 처리
    @Transactional
    public void readAll(User user) {
        validateNotificationEnabled(user);

        notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedDateDesc(user, Pageable.unpaged())
                .forEach(Notification::markAsRead);
    }

    private void validateNotificationEnabled(User user) {
        NotificationSetting setting = notificationSettingRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_SETTING_NOT_FOUND));

        if (!setting.isEnableNotification()) {
            throw new CustomException(ErrorCode.NOTIFICATION_DISABLED);
        }
    }

    private void validateOwner(Notification notification, User user) {
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
        }
    }

    // 읽지 않은 알림 개수
    public long getUnreadCount(User user) {
        validateNotificationEnabled(user);
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    // 알림 삭제
    @Transactional
    public void delete(Long notificationId, User user) {
        validateNotificationEnabled(user);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        validateOwner(notification, user);

        notificationRepository.delete(notification);
    }

    // 알림 설정 조회
    public NotificationSettingResponse getSetting(User user) {
        NotificationSetting setting = notificationSettingRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_SETTING_NOT_FOUND));

        return new NotificationSettingResponse(setting.isEnableNotification());
    }

    // 알림 설정 변경
    @Transactional
    public NotificationSettingResponse updateSetting(
            User user,
            NotificationSettingRequest request) {
        NotificationSetting setting = notificationSettingRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_SETTING_NOT_FOUND));

        if (request.enableNotification()) {
            setting.enableNotification();
        } else {
            setting.disableNotification();
        }

        return new NotificationSettingResponse(setting.isEnableNotification());
    }
}
