package com.team.snwa.snwabackend.domain.interest.controller;

import com.team.snwa.snwabackend.domain.interest.dto.response.InterestTargetResponse;
import com.team.snwa.snwabackend.domain.interest.dto.response.SubscriptionResponse;
import com.team.snwa.snwabackend.domain.interest.service.InterestService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;
    private final UserRepository userRepository;

    private Long getUserId(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }

    @GetMapping("/targets")
    public ResponseEntity<List<InterestTargetResponse>> searchTargets(@RequestParam String keyword) {
        return ResponseEntity.ok(interestService.searchTargets(keyword));
    }

    @PostMapping("/subscriptions/{targetId}")
    public ResponseEntity<Map<String, Boolean>> toggleSubscription(
            Principal principal,
            @PathVariable Long targetId) {
        return ResponseEntity.ok(interestService.toggleSubscription(getUserId(principal), targetId));
    }

    @GetMapping("/subscriptions/me")
    public ResponseEntity<List<SubscriptionResponse>> getMySubscriptions(Principal principal) {
        return ResponseEntity.ok(interestService.getMySubscriptions(getUserId(principal)));
    }
}
