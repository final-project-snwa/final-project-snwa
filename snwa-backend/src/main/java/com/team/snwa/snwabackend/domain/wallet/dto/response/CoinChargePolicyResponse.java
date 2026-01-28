package com.team.snwa.snwabackend.domain.wallet.dto.response;

import com.team.snwa.snwabackend.domain.wallet.entity.CoinChargePolicy;

public record CoinChargePolicyResponse(
        Long id,
        String name,
        Integer coinAmount,
        Integer price
) {
    public static CoinChargePolicyResponse from(CoinChargePolicy policy) {
        return new CoinChargePolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getCoinAmount(),
                policy.getPrice()
        );
    }
}
