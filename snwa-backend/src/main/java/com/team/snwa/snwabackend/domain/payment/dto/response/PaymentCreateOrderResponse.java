package com.team.snwa.snwabackend.domain.payment.dto.response;
public record PaymentCreateOrderResponse(
        String orderId,
        String orderName,
        Long amount
) {}
