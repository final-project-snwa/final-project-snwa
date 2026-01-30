package com.team.snwa.snwabackend.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.snwa.snwabackend.domain.order.entity.Order;
import com.team.snwa.snwabackend.domain.order.entity.OrderStatus;
import com.team.snwa.snwabackend.domain.order.repository.OrderRepository;
import com.team.snwa.snwabackend.domain.payment.dto.request.PaymentCancelRequest;
import com.team.snwa.snwabackend.domain.payment.dto.request.PaymentConfirmRequest;
import com.team.snwa.snwabackend.domain.payment.dto.response.PaymentCancelResponse;
import com.team.snwa.snwabackend.domain.payment.dto.response.PaymentResultResponse;
import com.team.snwa.snwabackend.domain.payment.dto.response.PaymentHistoryItemResponse;
import com.team.snwa.snwabackend.domain.payment.dto.response.PaymentHistoryResponse;
import com.team.snwa.snwabackend.domain.payment.entity.Payment;
import com.team.snwa.snwabackend.domain.payment.entity.PaymentCancel;
import com.team.snwa.snwabackend.domain.payment.entity.PaymentMethod;
import com.team.snwa.snwabackend.domain.payment.repository.PaymentCancelRepository;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final PaymentCancelRepository cancelRepo;

    private final TossPaymentsClient tossClient;
    private final ObjectMapper objectMapper;


    @Transactional
    public PaymentResultResponse confirm(PaymentConfirmRequest req) {

        // 1) 주문 row 락
        Order order = orderRepo.findByOrderIdForUpdate(req.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_ORDER_NOT_FOUND));
        // 2) 상태 가드
        // - PAID는 멱등 응답
        // - CANCELED/EXPIRED는 재승인 불가(정책)
        // - FAILED는 ✅ 재시도 허용(통과)
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new CustomException(ErrorCode.PAYMENT_ORDER_CANCELED);
        }
        if (order.getStatus() == OrderStatus.EXPIRED) {
            throw new CustomException(ErrorCode.PAYMENT_ORDER_EXPIRED);
        }

        // 3) 서버 DB 금액 검증(확정 실패) -> FAILED
        if (!order.getAmount().equals(req.amount())) {
            order.markFailed();
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 4) 이미 결제 완료면 멱등 처리
        if (order.isPaid()) {
            Payment existing = paymentRepo.findByOrderId(order.getOrderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_INCONSISTENT_STATE));
            return PaymentResultResponse.alreadyPaid(order.getOrderId(), existing);
        }

        // 5) 주문은 PAID가 아닌데 payment가 먼저 생긴 케이스(정합성 보정)
        Payment existingByOrder = paymentRepo.findByOrderId(order.getOrderId()).orElse(null);
        if (existingByOrder != null) {
            order.markPaid();
            return PaymentResultResponse.alreadyPaid(order.getOrderId(), existingByOrder);
        }

        // 6) 토스 승인 호출
        // ⚠️ 예외는 불확실 => FAILED 찍지 말고 PENDING/FAILED 그대로 둔다(재시도 가능하게)
        TossPaymentResponse tossRes;
        try {
            tossRes = tossClient.confirm(req.paymentKey(), req.orderId(), req.amount());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TOSS_CONFIRM_FAILED);
        }

        // 7) 토스 응답 검증(확정 실패) -> FAILED
        if (!req.orderId().equals(tossRes.orderId())) {
            order.markFailed();
            throw new CustomException(ErrorCode.TOSS_ORDER_ID_MISMATCH);
        }
        if (!req.amount().equals(tossRes.totalAmount())) {
            order.markFailed();
            throw new CustomException(ErrorCode.TOSS_AMOUNT_MISMATCH);
        }

        // 8) paymentKey 기반 멱등 처리
        Payment existedByKey = paymentRepo.findByPaymentKey(tossRes.paymentKey()).orElse(null);
        if (existedByKey != null) {
            order.markPaid();
            return PaymentResultResponse.alreadyPaid(order.getOrderId(), existedByKey);
        }

        // 9) 저장
        String raw = safeJson(tossRes);

        // ✅ tossRes.method(String) -> PaymentMethod enum 변환
        PaymentMethod method = PaymentMethod.fromToss(tossRes.method());

        Payment payment = Payment.create(
                order.getOrderId(),
                order.getUserId(),
                order.getOrderName(),
                tossRes.paymentKey(),
                method,                 // ✅ enum 저장
                tossRes.status(),
                tossRes.totalAmount(),
                tossRes.approvedAt(),
                raw
        );

        try {
            paymentRepo.save(payment);
        } catch (DataIntegrityViolationException e) {
            Payment existed = paymentRepo.findByPaymentKey(tossRes.paymentKey())
                    .orElseGet(() -> paymentRepo.findByOrderId(order.getOrderId())
                            .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_INCONSISTENT_STATE)));
            order.markPaid();
            return PaymentResultResponse.alreadyPaid(order.getOrderId(), existed);
        }

        // 10) 최종 상태 변경(저장 성공 후)
        order.markPaid();

        return PaymentResultResponse.success(order.getOrderId(), payment);
    }

    // 승인 취소
    public PaymentCancelResponse cancel(String paymentKey, PaymentCancelRequest req) {

        Payment payment = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        String orderId = payment.getOrderId();

        // ✅ 주문 row 락
        Order order = orderRepo.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        // 이미 취소된 주문이면 더 진행하지 않는다.
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_CANCELED);
        }

        // 검증: 취소금액 유효성/초과 방지
        Long sum = cancelRepo.sumCanceledAmountByPaymentKey(paymentKey);
        long totalCanceled = (sum == null) ? 0L : sum;

        if (req.cancelAmount() == null || req.cancelAmount() <= 0) {
            throw new CustomException(ErrorCode.INVALID_CANCEL_AMOUNT);
        }
        if (totalCanceled + req.cancelAmount() > order.getAmount()) {
            throw new CustomException(ErrorCode.CANCEL_AMOUNT_EXCEEDS_REMAINING);
        }

        TossCancelResponse tossRes;
        try {
            tossRes = tossClient.cancel(paymentKey, req.cancelReason(), req.cancelAmount());
        } catch (Exception e) {
            // ✅ 토스 취소 호출 실패는 "확정 실패"가 아니라 "불확실"
            throw new CustomException(ErrorCode.TOSS_CANCEL_FAILED);
        }

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

        // 전액 취소 여부 판정
        Long sumAfter = cancelRepo.sumCanceledAmountByPaymentKey(paymentKey);
        long totalCanceledAfter = (sumAfter == null) ? 0L : sumAfter;

        boolean fullyCanceled = totalCanceledAfter >= order.getAmount();
        if (fullyCanceled) {
            order.markCanceled();
        }

        return new PaymentCancelResponse(
                paymentKey,
                tossRes.cancelAmount(),
                totalCanceledAfter,
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

    // 결제내역 조회
    @Transactional(readOnly = true)
    public PaymentHistoryResponse getHistoryByUser(Long userId) {
        List<Payment> payments = paymentRepo.findAllByUserIdOrderByApprovedAtDesc(userId);

        List<PaymentHistoryItemResponse> items = payments.stream()
                .map(p -> new PaymentHistoryItemResponse(
                        p.getOrderId(),
                        p.getPaymentKey(),
                        p.getOrderName(),
                        p.getTotalAmount(),
                        p.getMethod().name(),
                        p.getTossStatus(),
                        p.getApprovedAt()
                ))
                .toList();

        return PaymentHistoryResponse.of(userId, items);
    }

    @Transactional(readOnly = true)
    public PaymentResultResponse getPaymentDetail(String paymentKey) {
        Payment payment = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        return PaymentResultResponse.success(payment.getOrderId(), payment);
    }
}
