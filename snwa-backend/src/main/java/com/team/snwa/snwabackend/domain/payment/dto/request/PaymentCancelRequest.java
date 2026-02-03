package com.team.snwa.snwabackend.domain.payment.dto.request;
import jakarta.validation.constraints.NotBlank;

public record PaymentCancelRequest(
        @NotBlank String cancelReason,
        Long cancelAmount //null => 전액취소
) {}