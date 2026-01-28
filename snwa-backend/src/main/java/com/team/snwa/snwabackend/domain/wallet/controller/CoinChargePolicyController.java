package com.team.snwa.snwabackend.domain.wallet.controller;

import com.team.snwa.snwabackend.domain.wallet.dto.response.CoinChargePolicyResponse;
import com.team.snwa.snwabackend.domain.wallet.service.CoinChargePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coins")
public class CoinChargePolicyController {
    private final CoinChargePolicyService coinChargePolicyService;

    @GetMapping("/policies")
    public List<CoinChargePolicyResponse> getPolicies() {
        return coinChargePolicyService.getActivePolicies();
    }
}
