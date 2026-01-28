package com.team.snwa.snwabackend.domain.payment.toss;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossCancelResponse(
        String paymentKey,
        String status,
        Long cancelAmount,
        LocalDateTime canceledAt
) {}
