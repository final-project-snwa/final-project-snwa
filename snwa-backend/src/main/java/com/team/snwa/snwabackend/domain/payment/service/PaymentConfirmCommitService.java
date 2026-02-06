package com.team.snwa.snwabackend.domain.payment.service;

import com.team.snwa.snwabackend.domain.order.entity.Order;
import com.team.snwa.snwabackend.domain.order.entity.OrderStatus;
import com.team.snwa.snwabackend.domain.order.repository.OrderRepository;
import com.team.snwa.snwabackend.domain.payment.entity.Payment;
import com.team.snwa.snwabackend.domain.payment.repository.PaymentRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentConfirmCommitService {

    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment savePaymentAndMarkPaid(Payment payment, @NotNull Long expectedAmount) {

        Order order = orderRepo.findByOrderIdForUpdate(payment.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        // 상태 재검증
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new CustomException(ErrorCode.PAYMENT_ORDER_CANCELED);
        }
        if (order.getStatus() == OrderStatus.EXPIRED) {
            throw new CustomException(ErrorCode.PAYMENT_ORDER_EXPIRED);
        }

        // amount 최종 검증(락 안에서)
        if (!order.getAmount().equals(expectedAmount)) {
            order.markFailed(); // ✅ 여기서 직접 변경 (OrderStatusService 호출 금지)
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 이미 PAID면 멱등
        if (order.isPaid()) {
            return paymentRepo.findByOrderId(order.getOrderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_INCONSISTENT_STATE));
        }

        // 멱등(중복 저장 방지)
        Payment existedByKey = paymentRepo.findByPaymentKey(payment.getPaymentKey()).orElse(null);
        if (existedByKey != null) {
            order.markPaid();
            return existedByKey;
        }

        Payment existedByOrder = paymentRepo.findByOrderId(payment.getOrderId()).orElse(null);
        if (existedByOrder != null) {
            order.markPaid();
            return existedByOrder;
        }

        try {
            Payment saved = paymentRepo.save(payment);
            order.markPaid();
            return saved;
        } catch (DataIntegrityViolationException e) {
            Payment existed = paymentRepo.findByPaymentKey(payment.getPaymentKey())
                    .orElseGet(() -> paymentRepo.findByOrderId(payment.getOrderId())
                            .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_INCONSISTENT_STATE)));
            order.markPaid();
            return existed;
        }
    }
}

