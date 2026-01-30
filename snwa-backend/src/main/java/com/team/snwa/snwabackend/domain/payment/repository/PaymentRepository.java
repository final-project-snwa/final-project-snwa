package com.team.snwa.snwabackend.domain.payment.repository;

import com.team.snwa.snwabackend.domain.payment.entity.Payment;
import com.team.snwa.snwabackend.domain.payment.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);
    boolean existsByPaymentKey(String paymentKey);

    // ✅ PAID 멱등 처리 시 안전 조회용 - order 1건당 payment 1건을 강제할 때
    Optional<Payment> findByOrder(PaymentOrder order);

    @Query("""
        select p
        from Payment p
        join p.order o
        where o.userId = :userId
        order by p.approvedAt desc nulls last, p.id desc
    """)
    List<Payment> findAllByUserIdOrderByApprovedAtDesc(@Param("userId") Long userId);
}