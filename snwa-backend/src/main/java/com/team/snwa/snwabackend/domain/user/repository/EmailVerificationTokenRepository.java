package com.team.snwa.snwabackend.domain.user.repository;

import com.team.snwa.snwabackend.domain.user.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByUser_Id(Long userId);
}
