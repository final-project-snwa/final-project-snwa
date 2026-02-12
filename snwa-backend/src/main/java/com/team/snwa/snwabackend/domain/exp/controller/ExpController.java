package com.team.snwa.snwabackend.domain.exp.controller;

import com.team.snwa.snwabackend.domain.exp.dto.LeaderboardEntryDto;
import com.team.snwa.snwabackend.domain.exp.dto.UserExpSummaryDto;
import com.team.snwa.snwabackend.domain.exp.service.LeaderboardService;
import com.team.snwa.snwabackend.domain.exp.service.UserExpService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exp")
@RequiredArgsConstructor
public class ExpController {
    private final LeaderboardService leaderboardService;
    private final UserExpService userExpService;
    private final UserRepository userRepository;

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> leaderboard(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(leaderboardService.getLeaderboard(Math.min(limit, 50)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserExpSummaryDto> myExp(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(userExpService.getMyExp(user.getId()));
    }
}
