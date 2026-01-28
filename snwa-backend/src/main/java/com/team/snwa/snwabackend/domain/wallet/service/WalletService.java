package com.team.snwa.snwabackend.domain.wallet.service;

import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.wallet.entity.Wallet;
import com.team.snwa.snwabackend.domain.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    //지갑이 있으면 조회, 없으면 생성하는 메서드
    @Transactional
    public Wallet getOrCreate(User user){
        return walletRepository.findByUser(user)
                .orElseGet(()->walletRepository.save(Wallet.create(user)));
    }

    //지갑 잔액 조회
    public Long getBalance(User user){
        return getOrCreate(user).getBalance();
    }
}
