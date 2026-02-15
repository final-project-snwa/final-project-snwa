package com.team.snwa.snwabackend.domain.wallet.dto.response;

import com.team.snwa.snwabackend.domain.exp.dto.ExpGrantInfoDto;

/**
 * 코인 사용 API 응답 (거래 정보 + 경험치 지급 정보)
 */
public record CoinUseResponse(
        CoinTransactionResponse transaction,
        ExpGrantInfoDto expGrantInfo
) {}
