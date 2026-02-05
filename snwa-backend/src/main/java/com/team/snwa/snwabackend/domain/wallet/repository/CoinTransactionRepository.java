package com.team.snwa.snwabackend.domain.wallet.repository;

import com.team.snwa.snwabackend.domain.wallet.entity.CoinTransaction;
import com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CoinTransactionRepository extends JpaRepository<CoinTransaction,Long> {
    //외부 식별자 기준 중복 트렌잭션 여부 확인
    boolean existsByUserIdAndExternalRef(Long userId, String externalRef);
    //사용자별 코인 내역 조회
    List<CoinTransaction> findByUserIdOrderByCreatedDateDesc(Long userId);
    //출석 보상 지급 여부 확인
    boolean existsByUserIdAndTypeAndCreatedDateBetween(
            Long userId,
            CoinTransactionType type,
            LocalDateTime start,
            LocalDateTime end
    );
    //유저가 이 기사에 대해 코인을 지불한 적이 있는지 판별
    boolean existsByUserIdAndTypeAndExternalRef(
            Long userId,
            CoinTransactionType type,
            String externalRef
    );

    Optional<CoinTransaction> findByUserIdAndExternalRef(Long userId, String externalRef);

    //이 결제(paymentKey)로 충전된 CHARGE 이후에
    //같은 유저의 SPEND 기록이 하나라도 있으면
    //이미 사용된 결제다
    @Query("""
    select count(tx) > 0
    from CoinTransaction tx
    where tx.userId = :userId
      and tx.type = com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionType.SPEND
      and tx.createdDate > (
          select c.createdDate
          from CoinTransaction c
          where c.userId = :userId
            and c.type = com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionType.CHARGE
            and c.externalRef = :paymentKey
      )
""")
    boolean existsSpendAfterCharge(
            Long userId,
            String paymentKey
    );

}
