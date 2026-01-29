package com.team.snwa.snwabackend.domain.payment.dto;

import com.team.snwa.snwabackend.domain.payment.entity.Payment;

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
    public static PaymentResultResponse success(String orderId, Payment payment) {
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

    public static PaymentResultResponse alreadyPaid(String orderId, Payment payment) {
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
