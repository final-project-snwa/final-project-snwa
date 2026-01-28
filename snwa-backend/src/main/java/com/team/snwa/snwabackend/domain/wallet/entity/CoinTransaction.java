package com.team.snwa.snwabackend.domain.wallet.entity;

import com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionStatus;
import com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionType;
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(
        name = "coin_transactions",
        uniqueConstraints = @UniqueConstraint(columnNames = "external_ref"),
        indexes = {
                @Index(name = "idx_coin_tx_user", columnList = "user_id, created_date"),
                @Index(name = "idx_coin_tx_external", columnList = "external_ref")
        })
@Getter
public class CoinTransaction extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    //코인 거래 유형(코인 충전/사용)
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private CoinTransactionType type;

    //거래 처리 상태(성공/실패)
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private CoinTransactionStatus status;

    //거래 코인 수량(항상 양수, 증감은 type으로 판단)
    @Column(nullable=false)
    private Long amount;

    //거래 이후 코인수 (계산 로직이 아닌 거래가 잘 처리되었는지 확인하기 위한 변수로 사용)
    @Column(name="balance_after")
    private Long balanceAfter;

    /**
     * 외부 시스템과의 연동 시 멱등성을 보장하기 위한 고유 참조값
     * - 결제 승인, 코인 충전 등 동일 요청이 여러 번 전달될 수 있는 상황을 방지
     * - 외부 시스템에서 전달받은 요청 식별자를 저장
     * - 동일 externalRef 요청은 한 번만 처리되도록 제어
     */
    @Column(name="external_ref")
    private String externalRef;

    protected CoinTransaction() {}

    /**
     * 코인 거래 기록을 생성함
     *
     * @param userId 거래 대상 사용자 식별자
     * @param type 코인 거래 유형
     * @param status 거래 처리 상태
     * @param amount 거래 코인 수량
     * @param balanceAfter 거래 이후 잔액
     * @param externalRef 외부 참조값
     * @return 생성된 CoinTransaction 엔티티
     */
    public static CoinTransaction create(
            Long userId,
            CoinTransactionType type,
            CoinTransactionStatus status,
            Long amount,
            Long balanceAfter,
            String externalRef
    ) {
        CoinTransaction tx = new CoinTransaction();
        tx.userId = userId;
        tx.type = type;
        tx.status = status;
        tx.amount = amount;
        tx.balanceAfter = balanceAfter;
        tx.externalRef = externalRef;
        return tx;
    }

}
