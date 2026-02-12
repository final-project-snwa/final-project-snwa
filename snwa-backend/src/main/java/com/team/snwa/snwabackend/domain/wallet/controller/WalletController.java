package com.team.snwa.snwabackend.domain.wallet.controller;

import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.domain.wallet.dto.request.CoinChargeRequest;
import com.team.snwa.snwabackend.domain.wallet.dto.request.CoinSpendRequest;
import com.team.snwa.snwabackend.domain.exp.dto.ExpGrantInfoDto;
import com.team.snwa.snwabackend.domain.exp.service.ExpGrantService;
import com.team.snwa.snwabackend.domain.wallet.dto.response.BalanceResponse;
import com.team.snwa.snwabackend.domain.wallet.dto.response.CoinTransactionResponse;
import com.team.snwa.snwabackend.domain.wallet.dto.response.CoinUseResponse;
import com.team.snwa.snwabackend.domain.wallet.service.CoinTransactionService;
import com.team.snwa.snwabackend.domain.wallet.service.WalletTransactionService;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coins")
public class WalletController {
    private final WalletTransactionService walletTransactionService;
    private final CoinTransactionService coinTransactionService;
    private final UserRepository userRepository;
    private final ExpGrantService expGrantService;

    /**
     * 코인 잔액 조회
     * Method: GET
     * Url: /api/coins/me
     */
    @GetMapping("/me")
    public BalanceResponse getMyCoinBalance(
            @AuthenticationPrincipal String email
    ){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return new BalanceResponse(walletTransactionService.getBalance(user));
    }

    /**
     * 코인 충전
     * Method: POST
     * Url: /api/coins/charge
     */
    @PostMapping("/charge")
    public CoinTransactionResponse charge(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody CoinChargeRequest request
    ){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return CoinTransactionResponse.from(
                walletTransactionService.charge(
                        user,
                        request.amount(),
                        request.externalRef()
                )
        );
    }


    /**
     * 코인 사용
     * Method: POST
     * Url: /api/coins/use
     * - 코인 1개당 경험치 20 지급
     */
    @PostMapping("/use")
    public CoinUseResponse useCoin(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody CoinSpendRequest request
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        var tx = walletTransactionService.spend(
                user,
                request.amount(),
                request.externalRef()
        );
        var expGrantInfo = expGrantService.grantCoinSpend(
                user.getId(), request.amount(), request.externalRef());
        return new CoinUseResponse(
                CoinTransactionResponse.from(tx),
                expGrantInfo != null ? new ExpGrantInfoDto(
                        expGrantInfo.expGained(), expGrantInfo.levelUp(), expGrantInfo.newLevel()) : null
        );
    }

    /**
     * 코인 사용/충전 내역 조회
     * Method: GET
     * Url: /api/coins/history
     */
    @GetMapping("/history")
    public List<CoinTransactionResponse> getCoinHistory(
            @AuthenticationPrincipal String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return coinTransactionService.getHistory(user.getId())
                .stream()
                .map(CoinTransactionResponse::from)
                .toList();
    }
}
