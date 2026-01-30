package com.team.snwa.snwabackend.domain.payment.dto.response;

import com.team.snwa.snwabackend.domain.payment.entity.Payment;
import com.team.snwa.snwabackend.domain.payment.entity.enums.PaymentMethod;

import java.time.LocalDateTime;

public record PaymentResultResponse(
        String orderId,
        String paymentKey,
        String tossStatus,
        PaymentMethod method,
        Long totalAmount,
        String approvedAt,
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
