package com.team.snwa.snwabackend.domain.payment.dto;
public record PaymentCreateOrderResponse(
        String orderId,
        String orderName,
        Long amount
) {}
