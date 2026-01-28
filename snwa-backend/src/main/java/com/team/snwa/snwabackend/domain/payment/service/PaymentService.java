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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
                .orElseThrow(() -> PaymentException.notFound("ORDER_NOT_FOUND", "order not found"));

        // 서버 DB 금액 검증(필수)
        if (!order.getAmount().equals(req.amount())) {
            order.markFailed();
            throw PaymentException.badRequest("AMOUNT_MISMATCH", "amount mismatch");
        }

        // 이미 PAID면 멱등 처리
        if (order.isPaid()) {
            // paymentKey가 다를 수도 있어서, 더 안전하게는 order->payment로 반환
            Payment existing = order.getPayment();
            if (existing == null) {
                throw PaymentException.conflict("PAID_BUT_PAYMENT_MISSING", "order is PAID but payment missing");
            }
            return PaymentResultResponse.alreadyPaid(order.getOrderId(), existing);
        }

        // 토스 승인 호출
        TossPaymentResponse tossRes = tossClient.confirm(req.paymentKey(), req.orderId(), req.amount());

        // 토스 응답 검증
        if (!req.orderId().equals(tossRes.orderId())) {
            order.markFailed();
            throw PaymentException.badRequest("TOSS_ORDERID_MISMATCH", "toss orderId mismatch");
        }
        if (!req.amount().equals(tossRes.totalAmount())) {
            order.markFailed();
            throw PaymentException.badRequest("TOSS_AMOUNT_MISMATCH", "toss amount mismatch");
        }

        // paymentKey 중복(멱등성/중복 승인 방지)
        if (paymentRepo.existsByPaymentKey(tossRes.paymentKey())) {
            // 이미 저장된 결제면 주문만 PAID로 보정하고 반환
            order.markPaid();
            Payment existed = paymentRepo.findByPaymentKey(tossRes.paymentKey())
                    .orElseThrow(() -> PaymentException.conflict("PAYMENT_EXISTS_BUT_NOT_FOUND", "payment exists but not found"));
            return PaymentResultResponse.alreadyPaid(order.getOrderId(), existed);
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
        paymentRepo.save(payment);

        return PaymentResultResponse.success(order.getOrderId(), payment);
    }

    public PaymentCancelResponse cancel(String paymentKey, PaymentCancelRequest req) {
        Payment payment = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> PaymentException.notFound("PAYMENT_NOT_FOUND", "payment not found"));

        PaymentOrder order = payment.getOrder();

        if (order.getStatus() == PaymentOrderStatus.CANCELED) {
            throw PaymentException.conflict("ALREADY_FULLY_CANCELED", "already fully canceled");
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

        // 누적 취소금액 계산 → 전액 취소 판단
        long totalCanceled = cancelRepo.sumCanceledAmountByPaymentKey(paymentKey);
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
        // 지금은 로그/저장만 해도 됨.
    }

    private String safeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}

