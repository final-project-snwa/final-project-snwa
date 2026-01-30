package com.team.snwa.snwabackend.domain.interest.dto.response;

import com.team.snwa.snwabackend.domain.interest.entity.InterestTarget;
import com.team.snwa.snwabackend.domain.interest.entity.InterestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class InterestTargetResponse {
    private Long id;
    private InterestType type;
    private String name;
    private String tagKey;

    public static InterestTargetResponse from(InterestTarget entity) {
        return InterestTargetResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .name(entity.getName())
                .tagKey(entity.getTagKey())
                .build();
    }
}
