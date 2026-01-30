package com.team.snwa.snwabackend.domain.payment.entity;

import com.team.snwa.snwabackend.domain.payment.common.BaseTimeEntity;
import com.team.snwa.snwabackend.domain.payment.entity.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_payment_key", columnNames = "payment_key")
                @UniqueConstraint(name = "uk_payments_order_id", columnNames = "order_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_key", nullable = false, length = 200)
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 50, nullable = false)
    private PaymentMethod method;

    @Column(nullable = false, length = 30)
    private String tossStatus;

    @Column(nullable = false)
    private Long totalAmount;

    private String approvedAt;

    @Lob
    private String rawJson;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "payment_order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payments_payment_order")
    )
    private PaymentOrder order;

    private Payment(PaymentOrder order, String paymentKey, PaymentMethod  method, String tossStatus,
                    Long totalAmount, String approvedAt, String rawJson) {
        this.order = order;
        this.paymentKey = paymentKey;
        this.method = method;
        this.tossStatus = tossStatus;
        this.totalAmount = totalAmount;
        this.approvedAt = approvedAt;
        this.rawJson = rawJson;
        
        //양방향 동기화
        order.attachPayment(this);
    }

    public static Payment create(PaymentOrder order,
                                 String paymentKey,
                                 PaymentMethod method,
                                 String tossStatus,
                                 Long totalAmount,
                                 String approvedAt,
                                 String rawJson) {
        return new Payment(order, paymentKey, method, tossStatus, totalAmount, approvedAt, rawJson);
    }
}
