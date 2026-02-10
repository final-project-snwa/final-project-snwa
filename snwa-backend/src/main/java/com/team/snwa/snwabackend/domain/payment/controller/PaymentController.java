package com.team.snwa.snwabackend.domain.payment.controller;



import com.team.snwa.snwabackend.domain.payment.dto.request.PaymentCancelRequest;
import com.team.snwa.snwabackend.domain.payment.dto.request.PaymentConfirmRequest;
import com.team.snwa.snwabackend.domain.payment.dto.response.*;
import com.team.snwa.snwabackend.domain.payment.service.PaymentService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;
    @PostMapping("/confirm")
    public PaymentResultResponse confirm(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody PaymentConfirmRequest req
    ) {
        if (email == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return paymentService.confirm(user.getId(), req);
    }

    @PostMapping("/{paymentKey}/cancel")
    public PaymentCancelResponse cancel(
            @AuthenticationPrincipal String email,
            @PathVariable String paymentKey,
            @Valid @RequestBody PaymentCancelRequest req
    ) {
        if (email == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return paymentService.cancel(user.getId(), paymentKey, req);
    }

    // 웹훅(수신만)
    @PostMapping("/webhook")
    public WebhookAckResponse webhook(@RequestBody String payload) {
        paymentService.handleWebhook(payload);
        return WebhookAckResponse.ok();
    }

    // 결제내역 조회(유저별)
    @GetMapping("/users/{userId}")
    public PaymentHistoryResponse history(@PathVariable Long userId) {
        return paymentService.getHistoryByUser(userId);
    }

    // 결제 단건 상세(결제키로)
    @GetMapping("/{paymentKey}")
    public PaymentResultResponse detail(@PathVariable String paymentKey) {
        return paymentService.getPaymentDetail(paymentKey);
    }
}
