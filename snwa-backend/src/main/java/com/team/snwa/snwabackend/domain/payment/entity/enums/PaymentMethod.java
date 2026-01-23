package com.team.snwa.snwabackend.domain.payment.entity.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CARD("카드"),
    TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE_PHONE("휴대폰");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }
}