package com.team.snwa.snwabackend.domain.article.controller;

import com.team.snwa.snwabackend.domain.article.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/articles/{articleId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * 좋아요 생성
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> addLike(
            @PathVariable Long articleId,
            @RequestParam Long userId
    ) {
        likeService.addLike(userId, articleId);
        return ResponseEntity.ok(Map.of("message", "좋아요가 추가되었습니다."));
    }

    /**
     * 좋아요 삭제
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> removeLike(
            @PathVariable Long articleId,
            @RequestParam Long userId
    ) {
        likeService.removeLike(userId, articleId);
        return ResponseEntity.ok(Map.of("message", "좋아요가 취소되었습니다."));
    }
}
