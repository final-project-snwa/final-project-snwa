package com.team.snwa.snwabackend.domain.payment.dto.response;

public record PaymentHistoryItemResponse(
        String orderId,
        String paymentKey,
        String orderName,
        Long amount,
        String method,
        String status,
        String approvedAt
) {
}
