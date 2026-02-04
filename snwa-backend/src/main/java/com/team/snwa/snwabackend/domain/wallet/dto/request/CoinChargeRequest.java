package com.team.snwa.snwabackend.domain.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CoinChargeRequest(
        @NotNull @Positive Long amount,
        @NotBlank String externalRef
) {
}
