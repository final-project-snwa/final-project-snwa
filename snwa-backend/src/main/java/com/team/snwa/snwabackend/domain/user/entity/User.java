package com.team.snwa.snwabackend.domain.user.entity;

import com.team.snwa.snwabackend.domain.user.entity.enums.UserRole;
import com.team.snwa.snwabackend.domain.user.entity.enums.UserStatus;
import com.team.snwa.snwabackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;  // 이메일이 곧 아이디

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 12)
    private String nickname;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.INACTIVE;  // 기본값: 비인증 상태

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;  // 이메일 인증 여부

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @Column(length = 255)
    private String introduction;

    @Column(length = 20)
    private String phoneNumber;

    // 소프트 삭제를 위한 필드
    @Column(nullable = true)
    private LocalDateTime deletedAt;

    // 소프트 삭제 메서드
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = UserStatus.DELETE;
    }

    public void updateProfile(String nickname, String introduction, String phoneNumber) {
        this.nickname = nickname;
        this.introduction = introduction;
        this.phoneNumber = phoneNumber;
    }

    // 회원 정보 수정 메서드
    public void updateInfo(String userEmail, String userNickname) {
        this.email = userEmail;
        this.nickname = userNickname;
    }

    // 프로필 이미지 수정(변경) 메서드
    public void updateImageUrl(String imageUrl) {
        this.profileImageUrl = imageUrl;
    }

    // 패스워드 재설정 메서드
    public void changePassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }

    // 이메일 인증 완료 메서드
    public void verifyEmail() {
        this.emailVerified = true;
        this.status = UserStatus.ACTIVE;
    }

}