package com.team.snwa.snwabackend.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.snwa.snwabackend.domain.order.entity.Order;
import com.team.snwa.snwabackend.domain.order.entity.OrderStatus;
import com.team.snwa.snwabackend.domain.order.repository.OrderRepository;
import com.team.snwa.snwabackend.domain.payment.dto.request.PaymentCancelRequest;
import com.team.snwa.snwabackend.domain.payment.dto.request.PaymentConfirmRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock OrderRepository orderRepo;
    @Mock PaymentRepository paymentRepo;
    @Mock PaymentCancelRepository cancelRepo;

    @Mock TossPaymentsClient tossClient;
    @Mock ObjectMapper objectMapper;

    @Mock WalletTransactionService walletTransactionService;
    @Mock EntityManager entityManager;

    @Mock
    PaymentConfirmCommitService paymentConfirmCommitService;
    @Mock
    PaymentCancelCommitService paymentCancelCommitService;

    @InjectMocks
    PaymentService paymentService;

    /* ---------------------------
       confirm 테스트
       --------------------------- */

    @Test
    void confirm_내주문아니면_FORBIDDEN() {
        Order order = Order.create(999L, "ORD_1", "충전", 10000L, 100, 1L);
        when(orderRepo.findByOrderId("ORD_1")).thenReturn(Optional.of(order));

        PaymentConfirmRequest req = new PaymentConfirmRequest("payKey", "ORD_1", 10000L);

        assertThatThrownBy(() -> paymentService.confirm(1L, req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void confirm_금액불일치_PAYMENT_AMOUNT_MISMATCH() {
        Order order = Order.create(1L, "ORD_1", "충전", 10000L, 100, 1L);
        when(orderRepo.findByOrderId("ORD_1")).thenReturn(Optional.of(order));

        PaymentConfirmRequest req = new PaymentConfirmRequest("payKey", "ORD_1", 9999L);

        assertThatThrownBy(() -> paymentService.confirm(1L, req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.PAYMENT_AMOUNT_MISMATCH));

        verifyNoInteractions(tossClient);
    }

    @Test
    void confirm_토스confirm실패_TOSS_CONFIRM_FAILED() {
        Order order = Order.create(1L, "ORD_1", "충전", 10000L, 100, 1L);
        when(orderRepo.findByOrderId("ORD_1")).thenReturn(Optional.of(order));

        PaymentConfirmRequest req = new PaymentConfirmRequest("payKey", "ORD_1", 10000L);

        when(tossClient.confirm("payKey", "ORD_1", 10000L)).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> paymentService.confirm(1L, req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.TOSS_CONFIRM_FAILED));
    }

    @Test
    void confirm_성공시_커밋서비스호출후_코인충전() {
        Order order = Order.create(1L, "ORD_1", "충전", 10000L, 100, 1L);
        when(orderRepo.findByOrderId("ORD_1")).thenReturn(Optional.of(order));

        PaymentConfirmRequest req = new PaymentConfirmRequest("payKey", "ORD_1", 10000L);

        TossPaymentResponse tossRes = mock(TossPaymentResponse.class);
        when(tossClient.confirm("payKey", "ORD_1", 10000L)).thenReturn(tossRes);

        when(tossRes.orderId()).thenReturn("ORD_1");
        when(tossRes.totalAmount()).thenReturn(10000L);
        when(tossRes.method()).thenReturn("CARD");
        when(tossRes.status()).thenReturn("DONE");
        when(tossRes.paymentKey()).thenReturn("payKey");
        when(tossRes.approvedAt()).thenReturn("2026-02-13T12:00:00");

        // objectMapper는 safeJson에서 실패해도 null 처리라 굳이 스텁 안해도 됨.
        when(entityManager.getReference(eq(User.class), eq(1L))).thenReturn(mock(User.class));

        Payment committed = Payment.create(
                "ORD_1", 1L, "충전",
                "payKey", PaymentMethod.CARD, "DONE",
                10000L, 100L,
                "2026-02-13T12:00:00", "{}"
        );
        when(paymentConfirmCommitService.savePaymentAndMarkPaid(any(Payment.class), eq(10000L)))
                .thenReturn(committed);

        var res = paymentService.confirm(1L, req);

        assertThat(res).isNotNull();

        verify(paymentConfirmCommitService).savePaymentAndMarkPaid(any(Payment.class), eq(10000L));
        verify(walletTransactionService).chargeIdempotent(any(User.class), eq(100L), eq("payKey"));
    }

    @Test
    void confirm_코인충전실패시_CHARGE_FAILED로상태업데이트하고_WALLET_CHARGE_FAILED() {
        Order order = Order.create(1L, "ORD_1", "충전", 10000L, 100, 1L);
        when(orderRepo.findByOrderId("ORD_1")).thenReturn(Optional.of(order));

        PaymentConfirmRequest req = new PaymentConfirmRequest("payKey", "ORD_1", 10000L);

        TossPaymentResponse tossRes = mock(TossPaymentResponse.class);
        when(tossClient.confirm("payKey", "ORD_1", 10000L)).thenReturn(tossRes);

        when(tossRes.orderId()).thenReturn("ORD_1");
        when(tossRes.totalAmount()).thenReturn(10000L);
        when(tossRes.method()).thenReturn("CARD");
        when(tossRes.status()).thenReturn("DONE");
        when(tossRes.paymentKey()).thenReturn("payKey");
        when(tossRes.approvedAt()).thenReturn("2026-02-13T12:00:00");

        when(entityManager.getReference(eq(User.class), eq(1L))).thenReturn(mock(User.class));

        Payment committed = Payment.create(
                "ORD_1", 1L, "충전",
                "payKey", PaymentMethod.CARD, "DONE",
                10000L, 100L,
                "2026-02-13T12:00:00", "{}"
        );
        when(paymentConfirmCommitService.savePaymentAndMarkPaid(any(Payment.class), eq(10000L)))
                .thenReturn(committed);

        doThrow(new RuntimeException("charge fail"))
                .when(walletTransactionService).chargeIdempotent(any(User.class), eq(100L), eq("payKey"));

        assertThatThrownBy(() -> paymentService.confirm(1L, req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.WALLET_CHARGE_FAILED));

        verify(orderRepo).updateStatus("ORD_1", OrderStatus.CHARGE_FAILED);
    }

    /* ---------------------------
       cancel 테스트
       --------------------------- */

    @Test
    void cancel_내결제아니면_FORBIDDEN() {
        Payment payment = Payment.create("ORD_1", 999L, "충전", "payKey",
                PaymentMethod.CARD, "DONE", 10000L, 100L, "t", "{}");
        when(paymentRepo.findByPaymentKey("payKey")).thenReturn(Optional.of(payment));

        PaymentCancelRequest req = new PaymentCancelRequest("사유", null);

        assertThatThrownBy(() -> paymentService.cancel(1L, "payKey", req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void cancel_부분취소요청이면_PARTIAL_CANCEL_NOT_ALLOWED() {
        Payment payment = Payment.create("ORD_1", 1L, "충전", "payKey",
                PaymentMethod.CARD, "DONE", 10000L, 100L, "t", "{}");
        when(paymentRepo.findByPaymentKey("payKey")).thenReturn(Optional.of(payment));

        Order order = Order.create(1L, "ORD_1", "충전", 10000L, 100, 1L);
        when(orderRepo.findByOrderId("ORD_1")).thenReturn(Optional.of(order));
        when(cancelRepo.sumCanceledAmountByPaymentKey("payKey")).thenReturn(0L);

        PaymentCancelRequest req = new PaymentCancelRequest("사유", 5000L);

        assertThatThrownBy(() -> paymentService.cancel(1L, "payKey", req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.PARTIAL_CANCEL_NOT_ALLOWED));

        verifyNoInteractions(tossClient);
    }

    @Test
    void cancel_성공시_토스취소후_커밋서비스호출하고_환불후_CANCEL_COMPLETED() {
        Payment payment = Payment.create("ORD_1", 1L, "충전", "payKey",
                PaymentMethod.CARD, "DONE", 10000L, 100L, "t", "{}");
        when(paymentRepo.findByPaymentKey("payKey")).thenReturn(Optional.of(payment));

        Order order = Order.create(1L, "ORD_1", "충전", 10000L, 100, 1L);
        when(orderRepo.findByOrderId("ORD_1")).thenReturn(Optional.of(order));

        when(cancelRepo.sumCanceledAmountByPaymentKey("payKey")).thenReturn(0L);

        // 코인 사용 안함 가정
        doNothing().when(walletTransactionService).assertNotUsedCharge(1L, "payKey");

        TossCancelResponse tossRes = mock(TossCancelResponse.class);
        when(tossClient.cancel("payKey", "사유", 10000L)).thenReturn(tossRes);
        when(tossRes.cancelAmount()).thenReturn(10000L);
        when(tossRes.canceledAt()).thenReturn(LocalDateTime.now());

        when(entityManager.getReference(eq(User.class), eq(1L))).thenReturn(mock(User.class));

        PaymentCancelRequest req = new PaymentCancelRequest("사유", null);

        var res = paymentService.cancel(1L, "payKey", req);

        assertThat(res).isNotNull();
        verify(paymentCancelCommitService).saveCancelAndMarkCanceled(
                eq("ORD_1"),
                eq(payment),
                eq(10000L),
                eq("사유"),
                any(LocalDateTime.class),
                any()
        );
        verify(walletTransactionService).refundIdempotent(any(User.class), eq(100L), eq("REFUND_payKey"));
        verify(orderRepo).updateStatus("ORD_1", OrderStatus.CANCEL_COMPLETED);
    }

    @Test
    void cancel_환불실패시_REFUND_FAILED로상태업데이트하고_WALLET_REFUND_FAILED() {
        Payment payment = Payment.create("ORD_1", 1L, "충전", "payKey",
                PaymentMethod.CARD, "DONE", 10000L, 100L, "t", "{}");
        when(paymentRepo.findByPaymentKey("payKey")).thenReturn(Optional.of(payment));

        Order order = Order.create(1L, "ORD_1", "충전", 10000L, 100, 1L);
        when(orderRepo.findByOrderId("ORD_1")).thenReturn(Optional.of(order));
        when(cancelRepo.sumCanceledAmountByPaymentKey("payKey")).thenReturn(0L);

        doNothing().when(walletTransactionService).assertNotUsedCharge(1L, "payKey");

        TossCancelResponse tossRes = mock(TossCancelResponse.class);
        when(tossClient.cancel("payKey", "사유", 10000L)).thenReturn(tossRes);
        when(tossRes.cancelAmount()).thenReturn(10000L);
        when(tossRes.canceledAt()).thenReturn(LocalDateTime.now());

        when(entityManager.getReference(eq(User.class), eq(1L))).thenReturn(mock(User.class));

        doThrow(new RuntimeException("refund fail"))
                .when(walletTransactionService).refundIdempotent(any(User.class), eq(100L), eq("REFUND_payKey"));

        PaymentCancelRequest req = new PaymentCancelRequest("사유", null);

        assertThatThrownBy(() -> paymentService.cancel(1L, "payKey", req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.WALLET_REFUND_FAILED));

        verify(orderRepo).updateStatus("ORD_1", OrderStatus.REFUND_FAILED);
    }
}
