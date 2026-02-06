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
            String orderId,
            Payment payment,
            Long cancelAmount,
            String cancelReason,
            LocalDateTime canceledAt,
            String rawJson
    ) {
        Order order = orderRepo.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        // ✅ 멱등: 이미 취소 기록이 있으면 더 안 만든다
        if (cancelRepo.existsByPaymentKey(payment.getPaymentKey())) {
            // 상태만 보정
            order.markCanceled();
            return;
        }

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

