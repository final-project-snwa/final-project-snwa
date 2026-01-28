package com.team.snwa.snwabackend.domain.wallet.repository;

import com.team.snwa.snwabackend.domain.wallet.entity.CoinTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoinTransactionRepository extends JpaRepository<CoinTransaction,Long> {
    //외부 식별자 기준 중복 트렌잭션 여부 확인
    boolean existsByExternalRef(String externalRef);
    //사용자별 코인 내역 조회
    List<CoinTransaction> findByUserIdOrderByCreatedDateDesc(Long userId);
}
