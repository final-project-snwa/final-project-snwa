package com.team.snwa.snwabackend.domain.payment.service;

import com.team.snwa.snwabackend.domain.order.entity.Order;
import com.team.snwa.snwabackend.domain.order.repository.OrderRepository;
import com.team.snwa.snwabackend.domain.payment.entity.Payment;
import com.team.snwa.snwabackend.domain.payment.repository.PaymentRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
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

    /**
     * ✅ "토스 승인 성공"을 DB에 확정 커밋하는 서비스
     * - outer transaction이 이후에 터져도(코인 지급 실패 등) payment/order는 살아남는다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment savePaymentAndMarkPaid(Payment payment) {

        // order 락 확보
        Order order = orderRepo.findByOrderIdForUpdate(payment.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        // 멱등(중복 커밋 방지)
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
            // 유니크 충돌이면 다시 조회해서 멱등 응답
            Payment existed = paymentRepo.findByPaymentKey(payment.getPaymentKey())
                    .orElseGet(() -> paymentRepo.findByOrderId(payment.getOrderId())
                            .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_INCONSISTENT_STATE)));

            order.markPaid();
            return existed;
        }
    }
}
