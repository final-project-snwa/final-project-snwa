package com.team.snwa.snwabackend.domain.user.entity;

import com.team.snwa.snwabackend.domain.user.entity.enums.UserRole;
import com.team.snwa.snwabackend.domain.user.entity.enums.UserStatus;
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") @Getter
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(unique = true)
    private String nickname;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    // 탈퇴일 (Soft Delete)
    private LocalDateTime deletedAt;

}