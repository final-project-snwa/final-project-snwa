package com.team.snwa.snwabackend.domain.wallet.repository;

import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    //사용자의 지갑 조회
    Optional<Wallet> findByUser(User user);
}
