package com.team.snwa.snwabackend.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.snwa.snwabackend.domain.order.entity.Order;
import com.team.snwa.snwabackend.domain.order.entity.OrderStatus;
import com.team.snwa.snwabackend.domain.order.service.OrderStatusService;
import com.team.snwa.snwabackend.domain.order.repository.OrderRepository;
import com.team.snwa.snwabackend.domain.payment.dto.request.PaymentCancelRequest;
import com.team.snwa.snwabackend.domain.payment.dto.request.PaymentConfirmRequest;
import com.team.snwa.snwabackend.domain.payment.dto.response.PaymentCancelResponse;
import com.team.snwa.snwabackend.domain.payment.dto.response.PaymentHistoryItemResponse;
import com.team.snwa.snwabackend.domain.payment.dto.response.PaymentHistoryResponse;
import com.team.snwa.snwabackend.domain.payment.dto.response.PaymentResultResponse;
import com.team.snwa.snwabackend.domain.payment.entity.Payment;
import com.team.snwa.snwabackend.domain.payment.entity.PaymentMethod;
import com.team.snwa.snwabackend.domain.payment.repository.PaymentCancelRepository;
import com.team.snwa.snwabackend.domain.payment.repository.PaymentRepository;
import com.team.snwa.snwabackend.domain.payment.toss.TossCancelResponse;
import com.team.snwa.snwabackend.domain.payment.toss.TossPaymentResponse;
import com.team.snwa.snwabackend.domain.payment.toss.TossPaymentsClient;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.wallet.service.WalletTransactionService;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final PaymentCancelRepository cancelRepo;

    private final TossPaymentsClient tossClient;
    private final ObjectMapper objectMapper;

    private final WalletTransactionService walletTransactionService;
    private final EntityManager entityManager;

    private final PaymentConfirmCommitService paymentConfirmCommitService;

    // ✅ 추가
    private final OrderStatusService orderStatusService;
    private final PaymentCancelCommitService paymentCancelCommitService;

    public PaymentResultResponse confirm(PaymentConfirmRequest req) {

        Order order = orderRepo.findByOrderIdForUpdate(req.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new CustomException(ErrorCode.PAYMENT_ORDER_CANCELED);
        }
        if (order.getStatus() == OrderStatus.EXPIRED) {
            throw new CustomException(ErrorCode.PAYMENT_ORDER_EXPIRED);
        }

        // ✅ 서버 기준 amount 검증 (불일치면 FAILED 확정)
        if (!order.getAmount().equals(req.amount())) {
            orderStatusService.markFailed(order.getOrderId());
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 이미 PAID면 멱등 처리
        if (order.isPaid()) {
            Payment existing = paymentRepo.findByOrderId(order.getOrderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_INCONSISTENT_STATE));
            return PaymentResultResponse.alreadyPaid(order.getOrderId(), existing);
        }

        long coinAmount = order.getCoinAmount().longValue();

        TossPaymentResponse tossRes;
        try {
            tossRes = tossClient.confirm(req.paymentKey(), req.orderId(), req.amount());
        } catch (Exception e) {
            // 불확실 실패 -> FAILED 확정 찍지 않음(재시도 허용)
            throw new CustomException(ErrorCode.TOSS_CONFIRM_FAILED);
        }

        // ✅ toss 응답 검증 (불일치면 FAILED 확정)
        if (!req.orderId().equals(tossRes.orderId())) {
            orderStatusService.markFailed(order.getOrderId());
            throw new CustomException(ErrorCode.TOSS_ORDER_ID_MISMATCH);
        }
        if (!req.amount().equals(tossRes.totalAmount())) {
            orderStatusService.markFailed(order.getOrderId());
            throw new CustomException(ErrorCode.TOSS_AMOUNT_MISMATCH);
        }

        String raw = safeJson(tossRes);
        PaymentMethod method = PaymentMethod.fromToss(tossRes.method());

        Payment payment = Payment.create(
                order.getOrderId(),
                order.getUserId(),
                order.getOrderName(),
                tossRes.paymentKey(),
                method,
                tossRes.status(),
                tossRes.totalAmount(),
                coinAmount,
                tossRes.approvedAt(),
                raw
        );

        // ✅ 여기서 “승인 성공 기록”을 무조건 커밋해버림
        Payment committed = paymentConfirmCommitService.savePaymentAndMarkPaid(payment);

        // ✅ 그 다음 코인 지급(실패할 수 있음)
        User userRef = entityManager.getReference(User.class, order.getUserId());
        try {
            walletTransactionService.chargeIdempotent(userRef, coinAmount, committed.getPaymentKey());
        } catch (Exception e) {
            // ✅ 결제는 성공했는데 코인 지급이 실패한 상태를 남김 (운영/재처리 가능)
            orderStatusService.markChargeFailed(order.getOrderId());


            throw new CustomException(ErrorCode.WALLET_CHARGE_FAILED);
        }

        return PaymentResultResponse.success(order.getOrderId(), committed);
    }

    public PaymentCancelResponse cancel(String paymentKey, PaymentCancelRequest req) {

        Payment payment = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        String orderId = payment.getOrderId();

        Order order = orderRepo.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_CANCELED);
        }

        Long sum = cancelRepo.sumCanceledAmountByPaymentKey(paymentKey);
        long totalCanceled = (sum == null) ? 0L : sum;

        long remaining = order.getAmount() - totalCanceled;
        if (remaining <= 0) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_CANCELED);
        }

        Long cancelAmount = req.cancelAmount();
        if (cancelAmount == null) {
            cancelAmount = remaining;
        }

        if (cancelAmount <= 0) {
            throw new CustomException(ErrorCode.INVALID_CANCEL_AMOUNT);
        }
        if (cancelAmount > remaining) {
            throw new CustomException(ErrorCode.CANCEL_AMOUNT_EXCEEDS_REMAINING);
        }
        if (cancelAmount != remaining) {
            throw new CustomException(ErrorCode.PARTIAL_CANCEL_NOT_ALLOWED);
        }

        // 토스 취소 전에 코인 미사용 체크
        walletTransactionService.assertNotUsedCharge(payment.getUserId(), paymentKey);

        // 토스 취소
        TossCancelResponse tossRes;
        try {
            tossRes = tossClient.cancel(paymentKey, req.cancelReason(), cancelAmount);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TOSS_CANCEL_FAILED);
        }

        // ✅ 토스 취소 성공 이후:
        // "취소 기록 + 주문취소"를 먼저 REQUIRES_NEW로 커밋
        String raw = safeJson(tossRes);
        paymentCancelCommitService.saveCancelAndMarkCanceled(
                orderId,
                payment,
                tossRes.cancelAmount(),
                req.cancelReason(),
                tossRes.canceledAt(),
                raw
        );

        // 그 다음 내부 환불(코인 회수)
        User userRef = entityManager.getReference(User.class, payment.getUserId());
        String refundRef = "REFUND_" + paymentKey;

        try {
            walletTransactionService.refundIdempotent(
                    userRef,
                    payment.getChargedCoinAmount(),
                    refundRef
            );

            orderStatusService.markCancelCompleted(orderId);

        } catch (Exception e) {
            orderStatusService.markRefundFailed(orderId);

            throw new CustomException(ErrorCode.WALLET_REFUND_FAILED);
        }

        Long sumAfter = cancelRepo.sumCanceledAmountByPaymentKey(paymentKey);
        long totalCanceledAfter = (sumAfter == null) ? 0L : sumAfter;

        return new PaymentCancelResponse(
                paymentKey,
                tossRes.cancelAmount(),
                totalCanceledAfter,
                true,
                tossRes.canceledAt()
        );
    }

    public void handleWebhook(String payload) { }

    private String safeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

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
