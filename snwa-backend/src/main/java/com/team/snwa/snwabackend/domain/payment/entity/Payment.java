package com.team.snwa.snwabackend.domain.payment.entity;

import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_payment_key", columnNames = "payment_key"),
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

    // ✅ Order 엔티티 대신 식별자만 저장
    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    // ✅ history 조회용(조인 제거)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ✅ 주문명도 스냅샷 저장(조인 제거)
    @Column(name = "order_name", nullable = false, length = 200)
    private String orderName;

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

    private Payment(String orderId, Long userId, String orderName,
                    String paymentKey, PaymentMethod method, String tossStatus,
                    Long totalAmount, String approvedAt, String rawJson) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderName = orderName;
        this.paymentKey = paymentKey;
        this.method = method;
        this.tossStatus = tossStatus;
        this.totalAmount = totalAmount;
        this.approvedAt = approvedAt;
        this.rawJson = rawJson;
    }

    public static Payment create(String orderId, Long userId, String orderName,
                                 String paymentKey, PaymentMethod method, String tossStatus,
                                 Long totalAmount, String approvedAt, String rawJson) {
        return new Payment(orderId, userId, orderName, paymentKey, method, tossStatus, totalAmount, approvedAt, rawJson);
    }
}
