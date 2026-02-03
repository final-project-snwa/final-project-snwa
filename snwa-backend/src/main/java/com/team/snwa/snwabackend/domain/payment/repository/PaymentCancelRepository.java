package com.team.snwa.snwabackend.domain.payment.repository;

import com.team.snwa.snwabackend.domain.payment.entity.PaymentCancel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentCancelRepository extends JpaRepository<PaymentCancel, Long> {

    @Query("""
           select sum(c.cancelAmount)
           from PaymentCancel c
           where c.payment.paymentKey = :paymentKey
           """)
    Long sumCanceledAmountByPaymentKey(@Param("paymentKey") String paymentKey);
}
