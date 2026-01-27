package com.team.snwa.snwabackend.domain.wallet.controller;

import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.wallet.dto.request.CoinChargeRequest;
import com.team.snwa.snwabackend.domain.wallet.dto.request.CoinSpendRequest;
import com.team.snwa.snwabackend.domain.wallet.dto.response.BalanceResponse;
import com.team.snwa.snwabackend.domain.wallet.dto.response.CoinTransactionResponse;
import com.team.snwa.snwabackend.domain.wallet.service.CoinTransactionService;
import com.team.snwa.snwabackend.domain.wallet.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coins")
public class WalletController {
    private final WalletTransactionService walletTransactionService;
    private final CoinTransactionService coinTransactionService;

    /**
     * 코인 잔액 조회
     * Method: GET
     * Url: /api/coins/me
     */
    @GetMapping("/me")
    public BalanceResponse getMyCoinBalance(
            @AuthenticationPrincipal User user
    ){
        return new BalanceResponse(walletTransactionService.getBalance(user));
    }

    /**
     * 코인 충전
     * Method: POST
     * Url: /api/coins/charge
     */
    @PostMapping("/charge")
    public CoinTransactionResponse charge(
            @AuthenticationPrincipal User user,
            @RequestBody CoinChargeRequest request
    ){
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
     */
    @PostMapping("/use")
    public CoinTransactionResponse useCoin(
            @AuthenticationPrincipal User user,
            @RequestBody CoinSpendRequest request
    ) {
        return CoinTransactionResponse.from(
                walletTransactionService.spend(
                        user,
                        request.amount(),
                        request.externalRef()
                )
        );
    }

    /**
     * 코인 사용/충전 내역 조회
     * Method: GET
     * Url: /api/coins/history
     */
    @GetMapping("/history")
    public List<CoinTransactionResponse> getCoinHistory(
            @AuthenticationPrincipal User user
    ) {
        return coinTransactionService.getHistory(user.getId())
                .stream()
                .map(CoinTransactionResponse::from)
                .toList();
    }
}
