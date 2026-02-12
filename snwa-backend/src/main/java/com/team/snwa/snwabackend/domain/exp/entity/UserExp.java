package com.team.snwa.snwabackend.domain.exp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_exp", indexes = {
        @Index(name = "idx_user_exp_status_total", columnList = "status, total_exp DESC"),
        @Index(name = "idx_user_exp_status_level", columnList = "status, level DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserExp {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_exp", nullable = false)
    @Builder.Default
    private long totalExp = 0;

    @Column(nullable = false)
    @Builder.Default
    private int level = 1;

    @Column(length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    public void addExp(int amount) {
        this.totalExp = Math.max(0, this.totalExp + amount);
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(99, level));
    }
}
