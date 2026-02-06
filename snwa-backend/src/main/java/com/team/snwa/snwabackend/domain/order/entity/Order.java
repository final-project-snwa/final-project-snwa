package com.team.snwa.snwabackend.domain.order.entity;



import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_orders_order_id", columnNames = "order_id")
        },
        indexes = {
                @Index(name = "idx_orders_user_id", columnList = "user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Toss orderId
    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_name", nullable = false, length = 200)
    private String orderName;

    // 결제 금액
    @Column(nullable = false)
    private Long amount;

    // 충전될 코인 수
    @Column(nullable = false)
    private Integer coinAmount;

    // 어떤 정책으로 충전했는지
    @Column(nullable = false)
    private Long policyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    private Order(Long userId,
                  String orderId,
                  String orderName,
                  Long amount,
                  Integer coinAmount,
                  Long policyId) {

        this.userId = userId;
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
        this.coinAmount = coinAmount;
        this.policyId = policyId;
        this.status = OrderStatus.PENDING;
    }

    public static Order create(Long userId,
                               String orderId,
                               String orderName,
                               Long amount,
                               Integer coinAmount,
                               Long policyId) {

        return new Order(userId, orderId, orderName, amount, coinAmount, policyId);
    }

    public boolean isPaid() {
        return status == OrderStatus.PAID;
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
    }

    public void markFailed() {
        this.status = OrderStatus.FAILED;
    }

    public void markCanceled() {
        this.status = OrderStatus.CANCELED;
    }

    // ✅ 내부 환불까지 완료
    public void markCancelCompleted() {
        this.status = OrderStatus.CANCEL_COMPLETED;
    }

    // ✅ 토스 취소 성공했는데 내부 환불 실패
    public void markRefundFailed() {
        this.status = OrderStatus.REFUND_FAILED;
    }

    public void markChargeFailed() {
        this.status = OrderStatus.CHARGE_FAILED;
    }
}
