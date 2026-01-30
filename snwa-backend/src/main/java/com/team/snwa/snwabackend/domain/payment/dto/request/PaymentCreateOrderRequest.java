package com.team.snwa.snwabackend.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCreateOrderRequest(
        @NotNull Long userId,
        @NotBlank String orderName,
        @NotNull Long amount
) {}
