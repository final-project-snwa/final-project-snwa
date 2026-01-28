package com.team.snwa.snwabackend.domain.wallet.entity;

import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(
        name = "wallets",
        uniqueConstraints = @UniqueConstraint(columnNames = "user_id")
)
@Getter
public class Wallet extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable=false)
    private Long balance;

    /**
     * 낙관적 락을 위한 버전 필드
     * - 동일 엔티티에 대한 동시 수정 시 충돌을 감지하기 위해 사용
     * - 업데이트 시 version 값을 비교하여 중간 변경이 발생한 경우 예외를 발생시킴
     * - 지갑 잔액, 코인 수량 등 동시성 문제가 발생할 수 있는 도메인에서 사용
     */
    @Version
    private Long version;

    protected  Wallet() {
    }

    /**
     * 사용자의 지갑을 최초로 생성함
     * 사용자당 하나의 지갑만 생성됨
     * 초기 잔액은 0으로 설정
     */
    public static Wallet create(User user) {
        Wallet wallet = new Wallet();
        wallet.user = user;
        wallet.balance = 0L;
        return wallet;
    }

    public void increase(Long amount) {
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.WALLET_AMOUNT_INVALID);
        }
        this.balance += amount;
    }

    public void decrease(Long amount) {
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.WALLET_AMOUNT_INVALID);
        }
        if (this.balance < amount) {
            throw new CustomException(ErrorCode.WALLET_INSUFFICIENT_BALANCE);
        }
        this.balance -= amount;
    }


}