package com.team.snwa.snwabackend.domain.payment.dto.response;

import java.util.List;

public record PaymentHistoryResponse(
        Long userId,
        List<PaymentHistoryItemResponse> items
) {
    public static PaymentHistoryResponse of(Long userId, List<PaymentHistoryItemResponse> items) {
        return new PaymentHistoryResponse(userId, items);
    }
}