package com.team.snwa.snwabackend.domain.order.dto.response;

public record OrderCreateResponse(
        String orderId,
        String orderName,
        Long amount
) {}