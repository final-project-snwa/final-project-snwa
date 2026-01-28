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
                @UniqueConstraint(name = "uk_payment_orders_order_id", columnNames = "orderId")
        },
        indexes = {
                @Index(name = "idx_payment_orders_user_id", columnList = "userId")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 토스 orderId (서버가 생성해서 프론트에 전달)
    @Column(nullable = false, length = 64)
    private String orderId;

    // MVP: 인증은 너희 프로젝트에 맞게 교체 (User 엔티티 FK로 바꿔도 됨)
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
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
}