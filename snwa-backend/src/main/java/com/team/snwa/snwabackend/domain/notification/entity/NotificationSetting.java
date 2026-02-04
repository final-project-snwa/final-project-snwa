package com.team.snwa.snwabackend.domain.notification.entity;

import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "notification_settings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_setting_user",
                        columnNames = "user_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //설정 주인
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //알림 사용 여부
    @Column(name = "enable_notification", nullable = false)
    private boolean enableNotification;


    private NotificationSetting(User user) {
        this.user = user;
        this.enableNotification = true;
    }

    public static NotificationSetting createDefault(User user) {
        return new NotificationSetting(user);
    }

    //알림설정켜기
    public void enableNotification() {
        this.enableNotification = true;
    }

    //알림설정끄기
    public void disableNotification() {
        this.enableNotification = false;
    }
}
