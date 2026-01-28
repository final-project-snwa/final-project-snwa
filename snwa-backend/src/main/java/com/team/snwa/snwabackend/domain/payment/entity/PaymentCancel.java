package com.team.snwa.snwabackend.domain.payment.entity;

import com.team.snwa.snwabackend.domain.payment.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "payment_cancels",
        indexes = {
                @Index(name = "idx_payment_cancels_payment_key", columnList = "paymentKey")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCancel extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 조회 편의용
    @Column(nullable = false, length = 200)
    private String paymentKey;

    @Column(nullable = false)
    private Long cancelAmount;

    @Column(nullable = false, length = 200)
    private String reason;

    private LocalDateTime canceledAt;

    @Lob
    private String rawJson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_cancels_payment"))
    private Payment payment;

    private PaymentCancel(Payment payment, Long cancelAmount, String reason, LocalDateTime canceledAt, String rawJson) {
        this.payment = payment;
        this.paymentKey = payment.getPaymentKey();
        this.cancelAmount = cancelAmount;
        this.reason = reason;
        this.canceledAt = canceledAt;
        this.rawJson = rawJson;
    }

    public static PaymentCancel create(Payment payment,
                                       Long cancelAmount,
                                       String reason,
                                       LocalDateTime canceledAt,
                                       String rawJson) {
        return new PaymentCancel(payment, cancelAmount, reason, canceledAt, rawJson);
    }
}
