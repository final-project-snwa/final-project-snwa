package com.team.snwa.snwabackend.domain.payment.dto;

import java.time.LocalDateTime;

public record PaymentCancelResponse(
        String paymentKey,
        Long cancelAmount,
        long totalCanceledAmount,
        boolean fullyCanceled,
        LocalDateTime canceledAt
) {}