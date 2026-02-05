package com.team.snwa.snwabackend.domain.user.service;

import com.team.snwa.snwabackend.domain.user.dto.request.SignupRequestDto;
import com.team.snwa.snwabackend.domain.user.dto.request.LoginRequestDto;
import com.team.snwa.snwabackend.domain.user.dto.request.ForgotPasswordRequestDto;
import com.team.snwa.snwabackend.domain.user.dto.request.ResetPasswordRequestDto;
import com.team.snwa.snwabackend.domain.user.entity.EmailVerificationToken;
import com.team.snwa.snwabackend.domain.user.entity.PasswordResetToken;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.entity.enums.UserStatus;
import com.team.snwa.snwabackend.domain.user.repository.EmailVerificationTokenRepository;
import com.team.snwa.snwabackend.domain.user.repository.PasswordResetTokenRepository;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.domain.wallet.service.WalletTransactionService;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import com.team.snwa.snwabackend.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final WalletTransactionService walletTransactionService;

    @Lazy
    @Autowired
    private AuthService self;

    @Transactional
    public void signup(SignupRequestDto request) {
        // 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .status(UserStatus.INACTIVE)
                .emailVerified(false)
                .build();

        // 사용자 저장 및 즉시 DB에 반영 (ID 생성 보장)
        user = userRepository.save(user);
        userRepository.flush(); // 즉시 DB에 반영하여 ID 확보

        // 이메일 인증 토큰 생성 및 발송
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(1)) // 24시간 유효
                .used(false)
                .build();

        tokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    @Transactional
    public String login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 이메일 인증 여부 확인
        if (!user.getEmailVerified()) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 활성 상태 확인
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new CustomException(ErrorCode.USER_INACTIVE);
        }

        // JWT 토큰 생성 (이메일을 subject로 사용) - 로그인 성공
        String token = jwtUtil.generateToken(user.getEmail());

        // 출석 보상은 별도 트랜잭션에서 실행 (실패해도 로그인에는 영향 없음)
        try {
            self.giveAttendanceRewardInNewTransaction(user.getId());
        } catch (Exception e) {
            log.warn("출석 보상 지급 실패 (userId={}, 무시): {}", user.getId(), e.getMessage());
        }

        return token;
    }

    /** 출석 보상을 새 트랜잭션에서 실행. 예외가 나도 로그인 트랜잭션은 롤백되지 않음 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void giveAttendanceRewardInNewTransaction(Long userId) {
        try {
            boolean rewarded = walletTransactionService.giveAttendanceRewardByUserId(userId);
            if (rewarded) {
                log.info("출석 보상 지급 완료 (userId={}, 1코인)", userId);
            } else {
                log.info("출석 보상 이미 지급됨 (userId={}, 오늘 재로그인)", userId);
            }
        } catch (Exception e) {
            log.warn("출석 보상 지급 실패 (userId={}): {}", userId, e.getMessage());
        }
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_VERIFICATION_TOKEN));

        if (verificationToken.isExpired()) {
            throw new CustomException(ErrorCode.EXPIRED_VERIFICATION_TOKEN);
        }

        if (verificationToken.getUsed()) {
            throw new CustomException(ErrorCode.ALREADY_VERIFIED_TOKEN);
        }

        User user = verificationToken.getUser();
        user.verifyEmail();
        verificationToken.markAsUsed();

        userRepository.save(user);
        tokenRepository.save(verificationToken);
    }

    public void logout(String token) {
        // JWT는 stateless이므로 클라이언트에서 토큰을 삭제하면 됩니다.
        // 필요하다면 Redis 등을 사용하여 블랙리스트를 관리할 수 있습니다.
        // 여기서는 단순히 성공 응답만 반환합니다.
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 기존 토큰 삭제 (이전 요청 무효화)
        passwordResetTokenRepository.deleteByUser_Id(user.getId());

        // 비밀번호 재설정 토큰 생성
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1)) // 1시간 유효
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new CustomException(ErrorCode.RESET_TOKEN_NOT_FOUND));

        if (resetToken.isExpired()) {
            throw new CustomException(ErrorCode.RESET_TOKEN_EXPIRED);
        }

        if (resetToken.getUsed()) {
            throw new CustomException(ErrorCode.RESET_TOKEN_ALREADY_USED);
        }

        User user = resetToken.getUser();
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        user.changePassword(encodedPassword);
        resetToken.markAsUsed();

        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);
    }
}
