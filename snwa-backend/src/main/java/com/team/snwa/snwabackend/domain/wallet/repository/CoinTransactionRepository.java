package com.team.snwa.snwabackend.domain.wallet.repository;

import com.team.snwa.snwabackend.domain.wallet.entity.CoinTransaction;
import com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionStatus;
import com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    select (count(ct) > 0)
    from CoinTransaction ct
    where ct.userId = :userId
      and ct.type = :spendType
      and ct.status = :successStatus
      and ct.createdDate > (
        select min(c2.createdDate)
        from CoinTransaction c2
        where c2.userId = :userId
          and c2.type = :chargeType
          and c2.status = :successStatus
          and c2.externalRef = :paymentKey
      )
    """)
    boolean existsSpendAfterCharge(
            @Param("userId") Long userId,
            @Param("paymentKey") String paymentKey,
            @Param("chargeType") CoinTransactionType chargeType,
            @Param("spendType") CoinTransactionType spendType,
            @Param("successStatus") CoinTransactionStatus successStatus
    );

    default boolean existsSpendAfterCharge(Long userId, String paymentKey) {
        return existsSpendAfterCharge(
                userId,
                paymentKey,
                CoinTransactionType.CHARGE,
                CoinTransactionType.SPEND,
                CoinTransactionStatus.SUCCESS
        );
    }
}
