package com.team.snwa.snwabackend.domain.payment.entity;

import com.team.snwa.snwabackend.domain.payment.entity.enums.PaymentMethod;
import com.team.snwa.snwabackend.domain.payment.entity.enums.PaymentStatus;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "payments")
@Getter
public class Payment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 결제한 사용자

    private String merchantUid; // 주문번호
    private Long amount; // 결제 금액
    private Long coinAmount; // 충전된 코인량

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // 결제 상태 (PAID, CANCELLED 등)

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private String pgProvider; // TODO: 열거형으로 수정 필요
}