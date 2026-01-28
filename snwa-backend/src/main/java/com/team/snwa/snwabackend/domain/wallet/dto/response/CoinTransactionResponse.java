package com.team.snwa.snwabackend.domain.wallet.dto.response;

import com.team.snwa.snwabackend.domain.wallet.entity.CoinTransaction;
import com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionStatus;
import com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionType;

import java.time.LocalDateTime;

/**
 * 코인 거래 내역 응답 DTO
 */
public record CoinTransactionResponse(
        Long id,
        CoinTransactionType type,      // SPEND / CHARGE
        CoinTransactionStatus status,  // SUCCESS / FAIL
        Long amount,
        Long balanceAfter,
        String externalRef,
        LocalDateTime createdAt
) {

    public static CoinTransactionResponse from(CoinTransaction tx) {
        return new CoinTransactionResponse(
                tx.getId(),
                tx.getType(),
                tx.getStatus(),
                tx.getAmount(),
                tx.getBalanceAfter(),
                tx.getExternalRef(),
                tx.getCreatedDate()
        );
    }
}