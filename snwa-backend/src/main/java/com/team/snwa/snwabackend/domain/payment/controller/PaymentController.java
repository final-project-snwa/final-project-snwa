package com.team.snwa.snwabackend.domain.payment.controller;



import com.team.snwa.snwabackend.domain.payment.dto.request.PaymentCancelRequest;
import com.team.snwa.snwabackend.domain.payment.dto.request.PaymentConfirmRequest;
import com.team.snwa.snwabackend.domain.payment.dto.request.PaymentCreateOrderRequest;
import com.team.snwa.snwabackend.domain.payment.dto.response.*;
import com.team.snwa.snwabackend.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    // 1) 주문 생성 (결제 전)
    @PostMapping("/prepare")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentCreateOrderResponse createOrder(@Valid @RequestBody PaymentCreateOrderRequest req) {
        return paymentService.createOrder(req);
    }

    // 2) 결제 승인 (successUrl에서 받은 paymentKey/orderId/amount로 호출)
    @PostMapping("/confirm")
    public PaymentResultResponse confirm(@Valid @RequestBody PaymentConfirmRequest req) {
        return paymentService.confirm(req);
    }

    // 3) 결제 취소(전액/부분)
    @PostMapping("/{paymentKey}/cancel")
    public PaymentCancelResponse cancel(@PathVariable String paymentKey,
                                        @Valid @RequestBody PaymentCancelRequest req) {
        return paymentService.cancel(paymentKey, req);
    }

    // 4) 웹훅(수신만)
    @PostMapping("/webhook")
    public WebhookAckResponse webhook(@RequestBody String payload) {
        paymentService.handleWebhook(payload);
        return WebhookAckResponse.ok();
    }

    // 5) 결제내역 조회(유저별)
    @GetMapping("/users/{userId}")
    public PaymentHistoryResponse history(@PathVariable Long userId) {
        return paymentService.getHistoryByUser(userId);
    }

    // 6) 결제 단건 상세(결제키로)
    @GetMapping("/{paymentKey}")
    public PaymentResultResponse detail(@PathVariable String paymentKey) {
        return paymentService.getPaymentDetail(paymentKey);
    }
}
