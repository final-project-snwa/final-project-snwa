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
    private OrderStatus status;

    private Order(Long userId, String orderId, String orderName, Long amount) {
        this.userId = userId;
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
        this.status = OrderStatus.PENDING;
    }

    public static Order create(Long userId, String orderId, String orderName, Long amount) {
        return new Order(userId, orderId, orderName, amount);
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
}