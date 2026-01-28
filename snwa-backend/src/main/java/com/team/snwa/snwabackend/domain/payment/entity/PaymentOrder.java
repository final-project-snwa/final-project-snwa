package com.team.snwa.snwabackend.domain.payment.entity;

import com.team.snwa.snwabackend.domain.payment.common.BaseTimeEntity;
import com.team.snwa.snwabackend.domain.payment.entity.enums.PaymentOrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "payment_orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_orders_order_id", columnNames = "order_id")
        },
        indexes = {
                @Index(name = "idx_payment_orders_user_id", columnList = "user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 토스 orderId
    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_name", nullable = false, length = 200)
    private String orderName;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentOrderStatus status;

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Payment payment;

    private PaymentOrder(Long userId, String orderId, String orderName, Long amount) {
        this.userId = userId;
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
        this.status = PaymentOrderStatus.PENDING;
    }

    public static PaymentOrder create(Long userId, String orderId, String orderName, Long amount) {
        return new PaymentOrder(userId, orderId, orderName, amount);
    }

    public boolean isPaid() {
        return status == PaymentOrderStatus.PAID;
    }

    public void markPaid() {
        this.status = PaymentOrderStatus.PAID;
    }

    public void markFailed() {
        this.status = PaymentOrderStatus.FAILED;
    }

    public void markCanceled() {
        this.status = PaymentOrderStatus.CANCELED;
    }

    //양방향 동기화 편의 메서드
    public void attachPayment(Payment payment) {
        this.payment = payment;
    }
}
