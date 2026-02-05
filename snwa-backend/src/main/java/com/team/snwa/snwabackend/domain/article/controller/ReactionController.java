package com.team.snwa.snwabackend.domain.article.controller;

import com.team.snwa.snwabackend.domain.article.dto.request.ReactionRequestDto;
import com.team.snwa.snwabackend.domain.article.dto.response.ReactionCountResponseDto;
import com.team.snwa.snwabackend.domain.article.entity.enums.ReactionType;
import com.team.snwa.snwabackend.domain.article.service.ReactionService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * 기사 감정 반응 컨트롤러
 */
@RestController
@RequestMapping("/api/articles/{articleId}/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;
    private final UserRepository userRepository;

    /** 이메일로 사용자 조회 */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /** Principal(이메일)이 있으면 User 조회, 없으면 null */
    private User resolveUser(Principal principal) {
        if (principal == null || principal.getName() == null) return null;
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }

    /**
     * 반응 추가/변경/취소 (토글)
     * - 같은 반응 클릭 시 취소
     * - 다른 반응 클릭 시 변경
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> toggleReaction(
            @PathVariable Long articleId,
            @Valid @RequestBody ReactionRequestDto requestDto,
            @AuthenticationPrincipal String email
    ) {
        User user = findUserByEmail(email);
        ReactionType resultType = reactionService.toggleReaction(user.getId(), articleId, requestDto.getReactionType());
        ReactionCountResponseDto counts = reactionService.getReactionCounts(articleId, user.getId());
        
        return ResponseEntity.ok(Map.of(
                "message", resultType != null ? "반응이 등록되었습니다." : "반응이 취소되었습니다.",
                "userReaction", resultType != null ? resultType.name() : "",
                "likeCount", counts.getLikeCount(),
                "dislikeCount", counts.getDislikeCount(),
                "sadCount", counts.getSadCount(),
                "angryCount", counts.getAngryCount()
        ));
    }

    /**
     * 반응 취소
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> removeReaction(
            @PathVariable Long articleId,
            @AuthenticationPrincipal String email
    ) {
        User user = findUserByEmail(email);
        reactionService.removeReaction(user.getId(), articleId);
        ReactionCountResponseDto counts = reactionService.getReactionCounts(articleId, user.getId());
        
        return ResponseEntity.ok(Map.of(
                "message", "반응이 취소되었습니다.",
                "likeCount", counts.getLikeCount(),
                "dislikeCount", counts.getDislikeCount(),
                "sadCount", counts.getSadCount(),
                "angryCount", counts.getAngryCount()
        ));
    }

    /**
     * 반응 개수 조회 (비로그인도 가능)
     */
    @GetMapping
    public ResponseEntity<ReactionCountResponseDto> getReactionCounts(
            @PathVariable Long articleId,
            Principal principal
    ) {
        User user = resolveUser(principal);
        Long userId = user != null ? user.getId() : null;
        ReactionCountResponseDto counts = reactionService.getReactionCounts(articleId, userId);
        return ResponseEntity.ok(counts);
    }
}
