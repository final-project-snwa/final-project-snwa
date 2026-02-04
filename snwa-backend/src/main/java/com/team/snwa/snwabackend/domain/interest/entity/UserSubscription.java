package com.team.snwa.snwabackend.domain.interest.entity;

import com.team.snwa.snwabackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_subscription", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "target_id" })
})
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private InterestTarget interestTarget;

    @Column(nullable = false)
    private boolean isAlarmOn;

    @Builder
    public UserSubscription(User user, InterestTarget interestTarget, boolean isAlarmOn) {
        this.user = user;
        this.interestTarget = interestTarget;
        this.isAlarmOn = isAlarmOn;
    }

    public void toggleAlarm() {
        this.isAlarmOn = !this.isAlarmOn;
    }
}
