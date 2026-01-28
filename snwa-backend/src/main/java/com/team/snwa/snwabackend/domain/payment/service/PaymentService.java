package com.team.snwa.snwabackend.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.snwa.snwabackend.domain.payment.dto.*;
import com.team.snwa.snwabackend.domain.payment.entity.Payment;
import com.team.snwa.snwabackend.domain.payment.entity.PaymentCancel;
import com.team.snwa.snwabackend.domain.payment.entity.PaymentOrder;
import com.team.snwa.snwabackend.domain.payment.entity.enums.PaymentOrderStatus;
import com.team.snwa.snwabackend.domain.payment.repository.PaymentCancelRepository;
import com.team.snwa.snwabackend.domain.payment.repository.PaymentOrderRepository;
import com.team.snwa.snwabackend.domain.payment.repository.PaymentRepository;
import com.team.snwa.snwabackend.domain.payment.toss.TossCancelResponse;
import com.team.snwa.snwabackend.domain.payment.toss.TossPaymentResponse;
import com.team.snwa.snwabackend.domain.payment.toss.TossPaymentsClient;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentOrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final PaymentCancelRepository cancelRepo;

    private final TossPaymentsClient tossClient;
    private final ObjectMapper objectMapper;

    public PaymentCreateOrderResponse createOrder(PaymentCreateOrderRequest req) {
        String orderId = "ORD-" + UUID.randomUUID();

        PaymentOrder order = PaymentOrder.create(
                req.userId(),
                orderId,
                req.orderName(),
                req.amount()
        );

        orderRepo.save(order);

        return new PaymentCreateOrderResponse(orderId, req.orderName(), req.amount());
    }

    public PaymentResultResponse confirm(PaymentConfirmRequest req) {
        PaymentOrder order = orderRepo.findByOrderId(req.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        // 서버 DB 금액 검증(필수)
        if (!order.getAmount().equals(req.amount())) {
            order.markFailed();
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // ✅ 이미 PAID면 "DB에서 Payment를 안전 조회"해서 멱등 처리
        if (order.isPaid()) {
            Payment existing = paymentRepo.findByOrder(order)
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_INCONSISTENT_STATE));
            return PaymentResultResponse.alreadyPaid(order.getOrderId(), existing);
        }

        // 토스 승인 호출
        TossPaymentResponse tossRes = tossClient.confirm(req.paymentKey(), req.orderId(), req.amount());

        // 토스 응답 검증
        if (!req.orderId().equals(tossRes.orderId())) {
            order.markFailed();
            throw new CustomException(ErrorCode.TOSS_ORDER_ID_MISMATCH);
        }
        if (!req.amount().equals(tossRes.totalAmount())) {
            order.markFailed();
            throw new CustomException(ErrorCode.TOSS_AMOUNT_MISMATCH);
        }

        // ✅ paymentKey 기반 멱등 처리 (우선 빠른 조회)
        Payment existedByKey = paymentRepo.findByPaymentKey(tossRes.paymentKey()).orElse(null);
        if (existedByKey != null) {
            order.markPaid(); // 주문 상태 보정
            return PaymentResultResponse.alreadyPaid(order.getOrderId(), existedByKey);
        }

        // 저장
        order.markPaid();
        String raw = safeJson(tossRes);

        Payment payment = Payment.create(
                order,
                tossRes.paymentKey(),
                tossRes.method(),
                tossRes.status(),
                tossRes.totalAmount(),
                tossRes.approvedAt(),
                raw
        );

        // ✅ 동시성 레이스 대응: 유니크 위반이면 기존 결제로 멱등 처리
        try {
            paymentRepo.save(payment);
        } catch (DataIntegrityViolationException e) {
            Payment existed = paymentRepo.findByPaymentKey(tossRes.paymentKey())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_INCONSISTENT_STATE));
            return PaymentResultResponse.alreadyPaid(order.getOrderId(), existed);
        }

        return PaymentResultResponse.success(order.getOrderId(), payment);
    }

    public PaymentCancelResponse cancel(String paymentKey, PaymentCancelRequest req) {
        Payment payment = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        PaymentOrder order = payment.getOrder();

        if (order.getStatus() == PaymentOrderStatus.CANCELED) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_CANCELED);
        }

        // 토스 취소 호출
        TossCancelResponse tossRes = tossClient.cancel(paymentKey, req.cancelReason(), req.cancelAmount());

        // 취소 이력 저장
        String raw = safeJson(tossRes);
        PaymentCancel cancel = PaymentCancel.create(
                payment,
                tossRes.cancelAmount(),
                req.cancelReason(),
                tossRes.canceledAt(),
                raw
        );
        cancelRepo.save(cancel);

        // ✅ 누적 취소금액 null 방어
        Long sum = cancelRepo.sumCanceledAmountByPaymentKey(paymentKey);
        long totalCanceled = (sum == null) ? 0L : sum;

        boolean fullyCanceled = totalCanceled >= order.getAmount();
        if (fullyCanceled) order.markCanceled();

        return new PaymentCancelResponse(
                paymentKey,
                tossRes.cancelAmount(),
                totalCanceled,
                fullyCanceled,
                tossRes.canceledAt()
        );
    }

    public void handleWebhook(String payload) {
        // MVP: 수신만 OK. 운영 단계에서 서명 검증/이벤트 처리 붙이기.
    }

    private String safeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
