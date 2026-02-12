package com.team.snwa.snwabackend.domain.user.controller;

import com.team.snwa.snwabackend.domain.user.dto.request.LoginRequestDto;
import com.team.snwa.snwabackend.domain.user.dto.request.SignupRequestDto;
import com.team.snwa.snwabackend.domain.user.dto.request.ForgotPasswordRequestDto;
import com.team.snwa.snwabackend.domain.user.dto.request.ResetPasswordRequestDto;
import com.team.snwa.snwabackend.domain.user.dto.response.AuthResponseDto;
import com.team.snwa.snwabackend.domain.user.dto.response.LoginResult;
import com.team.snwa.snwabackend.domain.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDto> signup(@Valid @RequestBody SignupRequestDto request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponseDto(null, "회원가입이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요.", null, null));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        var result = authService.login(request);
        return ResponseEntity.ok(new AuthResponseDto(
                result.token(), "로그인 성공", result.attendanceRewardGiven(), result.expGrantInfo()));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<AuthResponseDto> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(new AuthResponseDto(null, "이메일 인증이 완료되었습니다.", null, null));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponseDto> logout(@RequestHeader("Authorization") String authHeader) {
        // Authorization 헤더에서 토큰 추출 (Bearer 제거)
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok(new AuthResponseDto(null, "로그아웃 성공", null, null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponseDto> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(new AuthResponseDto(null, "비밀번호 재설정 링크가 이메일로 발송되었습니다.", null, null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponseDto> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new AuthResponseDto(null, "비밀번호가 성공적으로 재설정되었습니다.", null, null));
    }
}
