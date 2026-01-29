package com.team.snwa.snwabackend.domain.wallet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name="coin_charge_policy")
public class CoinChargePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique=true)
    private String name;

    @Column(nullable = false)
    private Integer coinAmount;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private boolean active=true;
}
