package com.team.snwa.snwabackend.domain.wallet.service;

import com.team.snwa.snwabackend.domain.wallet.entity.CoinTransaction;
import com.team.snwa.snwabackend.domain.wallet.repository.CoinTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class    CoinTransactionService {

    private final CoinTransactionRepository coinTransactionRepository;

    //중복 트렌잭션 여부 판별
    public boolean isDuplicate(String externalRef) {
        return coinTransactionRepository.existsByExternalRef(externalRef);
    }

    //트랙잭션 저장
    public CoinTransaction save(CoinTransaction tx) {
        return coinTransactionRepository.save(tx);
    }

    //내역 조회
    public List<CoinTransaction> getHistory(Long userId) {
        return coinTransactionRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }
}
