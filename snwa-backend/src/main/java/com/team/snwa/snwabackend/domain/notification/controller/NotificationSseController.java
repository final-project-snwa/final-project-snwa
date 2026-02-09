package com.team.snwa.snwabackend.domain.notification.controller;

import com.team.snwa.snwabackend.domain.notification.service.SseEmitterService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import com.team.snwa.snwabackend.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationSseController {

    private final SseEmitterService sseEmitterService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/subscribe")
    public SseEmitter subscribe(
            Principal principal,
            @RequestParam(value = "token", required = false) String token
    ) {
        User user = resolveUser(principal, token);
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return sseEmitterService.connect(user.getId());
    }

    private User resolveUser(Principal principal, String token) {
        if (principal != null && principal.getName() != null) {
            return userRepository.findByEmail(principal.getName()).orElse(null);
        }
        if (token == null || token.isBlank()) return null;
        if (!jwtUtil.validateToken(token)) return null;
        String email = jwtUtil.getEmailFromToken(token);
        if (email == null) return null;
        return userRepository.findByEmail(email).orElse(null);
    }
}
