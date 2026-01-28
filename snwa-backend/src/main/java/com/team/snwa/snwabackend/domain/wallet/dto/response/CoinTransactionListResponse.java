package com.team.snwa.snwabackend.domain.wallet.dto.response;

import java.util.List;

public record CoinTransactionListResponse(List<CoinTransactionResponse> transactions) {
}
