package com.team.snwa.snwabackend.domain.payment.repository;

import com.team.snwa.snwabackend.domain.payment.entity.PaymentOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByOrderId(String orderId);

    /**
     * 결제 승인(confirm) 처리 시 동시성(중복 요청) 방지를 위해
     * orderId 기준으로 주문 Row에 비관락(PESSIMISTIC_WRITE)을 걸고 조회한다.
     *
     * - 같은 orderId로 confirm이 동시에 여러 번 들어와도
     *   한 트랜잭션만 order row를 잡고 진행 → 나머지는 대기 후 "이미 결제됨" 분기로 빠짐
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PaymentOrder o where o.orderId = :orderId")
    Optional<PaymentOrder> findByOrderIdForUpdate(@Param("orderId") String orderId);
}