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
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_payment_key", columnNames = "paymentKey")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 토스 paymentKey
    @Column(nullable = false, length = 200)
    private String paymentKey;

    // 카드/간편결제 등 (토스가 주는 문자열)
    @Column(length = 50)
    private String method;

    // DONE, CANCELED 등 (토스 status 문자열)
    @Column(nullable = false, length = 30)
    private String tossStatus;

    @Column(nullable = false)
    private Long totalAmount;

    private LocalDateTime approvedAt;

    @Lob
    private String rawJson;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_order_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_payments_payment_order"))
    private PaymentOrder order;

    private Payment(PaymentOrder order, String paymentKey, String method, String tossStatus,
                    Long totalAmount, LocalDateTime approvedAt, String rawJson) {
        this.order = order;
        this.paymentKey = paymentKey;
        this.method = method;
        this.tossStatus = tossStatus;
        this.totalAmount = totalAmount;
        this.approvedAt = approvedAt;
        this.rawJson = rawJson;
    }

    public static Payment create(PaymentOrder order,
                                 String paymentKey,
                                 String method,
                                 String tossStatus,
                                 Long totalAmount,
                                 LocalDateTime approvedAt,
                                 String rawJson) {
        return new Payment(order, paymentKey, method, tossStatus, totalAmount, approvedAt, rawJson);
    }
}
