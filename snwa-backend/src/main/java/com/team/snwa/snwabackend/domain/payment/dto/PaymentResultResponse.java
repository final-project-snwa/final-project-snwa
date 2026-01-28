package com.team.snwa.snwabackend.domain.payment.dto;

import java.time.LocalDateTime;

public record PaymentResultResponse(
        String orderId,
        String paymentKey,
        String tossStatus,
        String method,
        Long totalAmount,
        LocalDateTime approvedAt,
        boolean alreadyPaid
) {
    public static PaymentResultResponse success(String orderId, com.team.payment.domain.Payment payment) {
        return new PaymentResultResponse(
                orderId,
                payment.getPaymentKey(),
                payment.getTossStatus(),
                payment.getMethod(),
                payment.getTotalAmount(),
                payment.getApprovedAt(),
                false
        );
    }

    public static PaymentResultResponse alreadyPaid(String orderId, com.team.payment.domain.Payment payment) {
        return new PaymentResultResponse(
                orderId,
                payment.getPaymentKey(),
                payment.getTossStatus(),
                payment.getMethod(),
                payment.getTotalAmount(),
                payment.getApprovedAt(),
                true
        );
    }
}
