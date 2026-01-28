package com.team.snwa.snwabackend.domain.payment.entity.enums;

import lombok.Getter;

@Getter
public enum PaymentOrderStatus {
    PENDING,     // 결제 생성됨 (인증 전)
    PAID,       // 결제 완료 (승인 성공)
    CANCELED,  // 결제 취소됨
    EXPIRED,   // 결제 시간 만료
    FAILED    // 결제 승인 실패
}