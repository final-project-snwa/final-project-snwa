package com.team.snwa.snwabackend.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.snwa.snwabackend.domain.order.entity.Order;
import com.team.snwa.snwabackend.domain.order.entity.OrderStatus;
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
    private final PaymentCancelCommitService paymentCancelCommitService;

    /**
     * ✅ 컨트롤러 시그니처에 맞춘 confirm
     */
    public PaymentResultResponse confirm(Long userId, PaymentConfirmRequest req) {

        Order order = orderRepo.findByOrderId(req.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        // ✅ 내 주문만 결제 승인 가능
        if (!order.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new CustomException(ErrorCode.PAYMENT_ORDER_CANCELED);
        }
        if (order.getStatus() == OrderStatus.EXPIRED) {
            throw new CustomException(ErrorCode.PAYMENT_ORDER_EXPIRED);
        }

        if (!order.getAmount().equals(req.amount())) {
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        if (order.isPaid()) {
            Payment existing = paymentRepo.findByOrderId(order.getOrderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_INCONSISTENT_STATE));
            return PaymentResultResponse.alreadyPaid(order.getOrderId(), existing);
        }

        long coinAmount = order.getCoinAmount().longValue();

        // ✅ 외부 호출(락 잡지 말기)
        TossPaymentResponse tossRes;
        try {
            tossRes = tossClient.confirm(req.paymentKey(), req.orderId(), req.amount());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TOSS_CONFIRM_FAILED);
        }

        if (!req.orderId().equals(tossRes.orderId())) {
            throw new CustomException(ErrorCode.TOSS_ORDER_ID_MISMATCH);
        }
        if (!req.amount().equals(tossRes.totalAmount())) {
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
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

        // ✅ "결제 저장 + 주문 paid" 는 커밋 서비스에서 (여기서만 락/정합성 처리)
        Payment committed = paymentConfirmCommitService.savePaymentAndMarkPaid(payment, req.amount());

        // ✅ 코인 충전(후처리)
        User userRef = entityManager.getReference(User.class, order.getUserId());
        try {
            walletTransactionService.chargeIdempotent(userRef, coinAmount, committed.getPaymentKey());
        } catch (Exception e) {
            orderRepo.updateStatus(order.getOrderId(), OrderStatus.CHARGE_FAILED);
            throw new CustomException(ErrorCode.WALLET_CHARGE_FAILED);
        }

        return PaymentResultResponse.success(order.getOrderId(), committed);
    }

    /**
     * ✅ 컨트롤러 시그니처에 맞춘 cancel
     */
    public PaymentCancelResponse cancel(Long userId, String paymentKey, PaymentCancelRequest req) {

        Payment payment = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // ✅ 내 결제만 취소 가능
        if (!payment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        String orderId = payment.getOrderId();

        // ✅ 락 없이 조회 (락 잡고 외부 API 호출 금지)
        Order order = orderRepo.findByOrderId(orderId)
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
        if (cancelAmount == null) cancelAmount = remaining;

        if (cancelAmount <= 0) throw new CustomException(ErrorCode.INVALID_CANCEL_AMOUNT);
        if (cancelAmount > remaining) throw new CustomException(ErrorCode.CANCEL_AMOUNT_EXCEEDS_REMAINING);

        // 부분취소 금지
        if (!cancelAmount.equals(remaining)) throw new CustomException(ErrorCode.PARTIAL_CANCEL_NOT_ALLOWED);

        // ✅ 토스 취소 전에 코인 미사용 체크 (락 없이)
        walletTransactionService.assertNotUsedCharge(payment.getUserId(), paymentKey);

        // ✅ 토스 취소(외부 호출) - 락 없이
        TossCancelResponse tossRes;
        try {
            tossRes = tossClient.cancel(paymentKey, req.cancelReason(), cancelAmount);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TOSS_CANCEL_FAILED);
        }

        // ✅ 토스 취소 성공 후에만 "취소 기록 + 주문취소" 커밋 (여기서만 락)
        String raw = safeJson(tossRes);
        paymentCancelCommitService.saveCancelAndMarkCanceled(
                orderId,
                payment,
                tossRes.cancelAmount(),
                req.cancelReason(),
                tossRes.canceledAt(),
                raw
        );

        // ✅ 내부 환불(코인 회수)
        User userRef = entityManager.getReference(User.class, payment.getUserId());
        String refundRef = "REFUND_" + paymentKey;

        try {
            walletTransactionService.refundIdempotent(
                    userRef,
                    payment.getChargedCoinAmount(),
                    refundRef
            );
            orderRepo.updateStatus(orderId, OrderStatus.CANCEL_COMPLETED);

        } catch (Exception e) {
            orderRepo.updateStatus(orderId, OrderStatus.REFUND_FAILED);
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

    /**
     * ✅ (권장) history/detail도 원래는 로그인 유저 기준으로 제한하는 게 맞음.
     * 지금 컨트롤러가 userId/paymentKey를 그냥 열어놔서,
     * 최소한 Service는 "요청자 == 대상" 방어는 해두는 게 안전함.
     */
    @Transactional(readOnly = true)
    public PaymentHistoryResponse getHistoryByUser(Long requesterId, Long userId) {
        if (!requesterId.equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

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
    public PaymentResultResponse getPaymentDetail(Long requesterId, String paymentKey) {
        Payment payment = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getUserId().equals(requesterId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return PaymentResultResponse.success(payment.getOrderId(), payment);
    }

    /* =========================
       🔻 호환용(기존 호출처 대비)
       ========================= */

    /** @deprecated 컨트롤러는 confirm(userId, req) 쓰도록 */
    @Deprecated
    public PaymentResultResponse confirm(PaymentConfirmRequest req) {
        throw new CustomException(ErrorCode.PAYMENT_NOT_FOUND);
    }

    /** @deprecated 컨트롤러는 cancel(userId, paymentKey, req) 쓰도록 */
    @Deprecated
    public PaymentCancelResponse cancel(String paymentKey, PaymentCancelRequest req) {
        throw new CustomException(ErrorCode.PAYMENT_NOT_FOUND);
    }

    /** @deprecated 컨트롤러는 getHistoryByUser(requesterId, userId) 쓰도록 */
    @Deprecated
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

    /** @deprecated 컨트롤러는 getPaymentDetail(requesterId, paymentKey) 쓰도록 */
    @Deprecated
    @Transactional(readOnly = true)
    public PaymentResultResponse getPaymentDetail(String paymentKey) {
        Payment payment = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        return PaymentResultResponse.success(payment.getOrderId(), payment);
    }
}
