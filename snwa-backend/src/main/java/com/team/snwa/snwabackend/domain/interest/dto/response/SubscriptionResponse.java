package com.team.snwa.snwabackend.domain.interest.dto.response;

import com.team.snwa.snwabackend.domain.interest.entity.UserSubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private InterestTargetResponse target;
    private boolean isAlarmOn;

    public static SubscriptionResponse from(UserSubscription entity) {
        return SubscriptionResponse.builder()
                .id(entity.getId())
                .target(InterestTargetResponse.from(entity.getInterestTarget()))
                .isAlarmOn(entity.isAlarmOn())
                .build();
    }
}
