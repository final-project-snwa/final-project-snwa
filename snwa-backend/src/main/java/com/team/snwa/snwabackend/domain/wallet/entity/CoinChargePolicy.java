package com.team.snwa.snwabackend.domain.wallet.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "coin_charge_policy",
        uniqueConstraints = {
                // ✅ 가격으로 정책 매칭한다면 반드시 유니크 권장
                @UniqueConstraint(name = "uk_coin_charge_policy_price", columnNames = "price"),
                // (선택) 이름 중복도 막고 싶으면 유지
                @UniqueConstraint(name = "uk_coin_charge_policy_name", columnNames = "name")
        },
        indexes = {
                // 정책 조회 시 active 조건을 자주 쓰면 도움이 됨 (row 적으면 없어도 됨)
                @Index(name = "idx_coin_charge_policy_active", columnList = "active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoinChargePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // name은 운영에서 바뀔 수 있어서 보통은 유니크가 필수는 아닌데,
    // 너희는 이미 UNIQUE(name)로 DB가 생성돼 있어서 그대로 맞춤.
    @Column(nullable = false, length = 255)
    private String name;

    // ✅ 실제 DB 컬럼이 coin_amount라서 명시적으로 매핑
    @Column(name = "coin_amount", nullable = false)
    private Integer coinAmount;

    // ✅ 결제 연동이 price 기반이면 유니크 필수
    @Column(nullable = false)
    private Integer price;

    // boolean -> MySQL bit(1)로 이미 생성되어 있음
    @Column(nullable = false)
    private boolean active = true;

    // 필요하면 생성용 팩토리(정책 시드/관리용)
    public static CoinChargePolicy create(String name, int coinAmount, int price, boolean active) {
        CoinChargePolicy p = new CoinChargePolicy();
        p.name = name;
        p.coinAmount = coinAmount;
        p.price = price;
        p.active = active;
        return p;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
