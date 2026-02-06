package com.team.snwa.snwabackend.domain.wallet.repository;

import com.team.snwa.snwabackend.domain.wallet.entity.CoinChargePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoinChargePolicyRepository extends JpaRepository<CoinChargePolicy, Long> {
    List<CoinChargePolicy> findByActiveTrue();

    Optional<CoinChargePolicy> findByPriceAndActiveTrue(Integer price);
}
