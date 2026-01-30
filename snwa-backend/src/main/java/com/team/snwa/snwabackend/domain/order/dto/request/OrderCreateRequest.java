package com.team.snwa.snwabackend.domain.order.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderCreateRequest(
        @NotNull Long userId,
        @NotBlank String orderName,
        @NotNull Long amount
) {}