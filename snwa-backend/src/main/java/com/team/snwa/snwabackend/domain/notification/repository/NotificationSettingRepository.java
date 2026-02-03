package com.team.snwa.snwabackend.domain.notification.repository;

import com.team.snwa.snwabackend.domain.notification.entity.NotificationSetting;
import com.team.snwa.snwabackend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingRepository
        extends JpaRepository<NotificationSetting, Long> {

    //유저의 알림 설정 조회
    Optional<NotificationSetting> findByUser(User user);
}