package com.team.snwa.snwabackend.domain.payment.repository;

import com.team.snwa.snwabackend.domain.payment.entity.PaymentCancel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentCancelRepository extends JpaRepository<PaymentCancel, Long> {

    @Query("select coalesce(sum(pc.cancelAmount), 0) from PaymentCancel pc where pc.paymentKey = :paymentKey")
    long sumCanceledAmountByPaymentKey(@Param("paymentKey") String paymentKey);
}
