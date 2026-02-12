package com.team.snwa.snwabackend.domain.exp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "exp_grant_history", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "grant_key"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ExpGrantHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "grant_key", nullable = false, length = 100)
    private String grantKey;

    @Column(nullable = false)
    private int expAmount;

    @Column(nullable = false, length = 50)
    private String grantType;

    private LocalDateTime createdAt;
}
