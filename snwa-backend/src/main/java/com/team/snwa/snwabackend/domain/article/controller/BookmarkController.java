package com.team.snwa.snwabackend.domain.article.controller;

import com.team.snwa.snwabackend.domain.article.dto.ArticleListResponseDto;
import com.team.snwa.snwabackend.domain.article.service.BookmarkService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 즐겨찾기 추가
     * @param articleId 기사 ID
     * @param user 인증된 사용자
     * @return 성공 응답
     */
    @PostMapping("/{articleId}")
    public ResponseEntity<Map<String, String>> addBookmark(
            @PathVariable Long articleId,
            @AuthenticationPrincipal User user
    ) {
        bookmarkService.addBookmark(user, articleId);
        return ResponseEntity.ok(Map.of("message", "즐겨찾기가 추가되었습니다."));
    }

    /**
     * 즐겨찾기 삭제
     * @param articleId 기사 ID
     * @param user 인증된 사용자
     * @return 성공 응답
     */
    @DeleteMapping("/{articleId}")
    public ResponseEntity<Map<String, String>> removeBookmark(
            @PathVariable Long articleId,
            @AuthenticationPrincipal User user
    ) {
        bookmarkService.removeBookmark(user, articleId);
        return ResponseEntity.ok(Map.of("message", "즐겨찾기가 삭제되었습니다."));
    }

    /**
     * 내 즐겨찾기 목록 조회
     * @param user 인증된 사용자
     * @param pageable 페이지 정보 (기본값: size=10, sort=createdDate,desc)
     * @return 즐겨찾기한 기사 목록
     */
    @GetMapping("/me")
    public ResponseEntity<Page<ArticleListResponseDto>> getMyBookmarks(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ArticleListResponseDto> bookmarks = bookmarkService.getBookmarks(user, pageable);
        return ResponseEntity.ok(bookmarks);
    }

    /**
     * 특정 기사의 즐겨찾기 여부 확인
     * @param articleId 기사 ID
     * @param user 인증된 사용자
     * @return 즐겨찾기 여부
     */
    @GetMapping("/{articleId}/check")
    public ResponseEntity<Map<String, Boolean>> checkBookmark(
            @PathVariable Long articleId,
            @AuthenticationPrincipal User user
    ) {
        boolean isBookmarked = bookmarkService.isBookmarked(user, articleId);
        return ResponseEntity.ok(Map.of("isBookmarked", isBookmarked));
    }

    /**
     * 내 즐겨찾기 개수 조회
     * @param user 인증된 사용자
     * @return 즐겨찾기 개수
     */
    @GetMapping("/me/count")
    public ResponseEntity<Map<String, Long>> getMyBookmarkCount(
            @AuthenticationPrincipal User user
    ) {
        long count = bookmarkService.getBookmarkCount(user);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
