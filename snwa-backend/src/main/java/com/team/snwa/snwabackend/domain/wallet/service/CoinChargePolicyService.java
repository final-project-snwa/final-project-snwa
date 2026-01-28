package com.team.snwa.snwabackend.domain.wallet.service;

import com.team.snwa.snwabackend.domain.wallet.dto.response.CoinChargePolicyResponse;
import com.team.snwa.snwabackend.domain.wallet.repository.CoinChargePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoinChargePolicyService {
    private final CoinChargePolicyRepository coinChargePolicyRepository;

    //활성화된 코인 정책 조회
    public List<CoinChargePolicyResponse> getActivePolicies() {
        return coinChargePolicyRepository.findByActiveTrue()
                .stream()
                .map(CoinChargePolicyResponse::from)
                .toList();
    }
}
