package com.team.snwa.snwabackend.domain.payment.entity.enums;

import lombok.Getter;

import java.util.Arrays;

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

    public static PaymentMethod fromToss(String tossMethod) {
        if (tossMethod == null || tossMethod.isBlank()) {
            throw new IllegalArgumentException("toss method is null/blank");
        }

        // 1) enum name 매칭 (CARD, TRANSFER ...)
        try {
            return PaymentMethod.valueOf(tossMethod);
        } catch (IllegalArgumentException ignore) {
        }

        // 2) description 매칭 (카드, 계좌이체 ...)
        return Arrays.stream(values())
                .filter(v -> v.description.equals(tossMethod))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown toss method: " + tossMethod));
    }
}
