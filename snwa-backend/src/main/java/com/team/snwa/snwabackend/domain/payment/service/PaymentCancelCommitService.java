package com.team.snwa.snwabackend.domain.payment.service;

import com.team.snwa.snwabackend.domain.order.entity.Order;
import com.team.snwa.snwabackend.domain.order.repository.OrderRepository;
import com.team.snwa.snwabackend.domain.payment.entity.Payment;
import com.team.snwa.snwabackend.domain.payment.entity.PaymentCancel;
import com.team.snwa.snwabackend.domain.payment.repository.PaymentCancelRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentCancelCommitService {

    private final OrderRepository orderRepo;
    private final PaymentCancelRepository cancelRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCancelAndMarkCanceled(
            String orderId,               // 1
            Payment payment,              // 2
            Long cancelAmount,            // 3
            String cancelReason,          // 4
            LocalDateTime canceledAt,     // 5 ✅
            String rawJson                // 6 ✅
    ) {
        Order order = orderRepo.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        PaymentCancel cancel = PaymentCancel.create(
                payment,
                cancelAmount,
                cancelReason,
                canceledAt,
                rawJson
        );

        cancelRepo.save(cancel);

        order.markCanceled();
    }
}
