package com.team.snwa.snwabackend.domain.wallet.entity;

import com.team.snwa.snwabackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "wallets")
@Getter
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Long balance;
}