package com.team.snwa.snwabackend.domain.article.controller;

import com.team.snwa.snwabackend.domain.article.dto.ArticleListResponseDto;
import com.team.snwa.snwabackend.domain.article.service.BookmarkService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final UserRepository userRepository;

    private User getCurrentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 즐겨찾기 추가
     * @param articleId 기사 ID
     * @param principal 인증된 사용자 (이메일)
     * @return 성공 응답
     */
    @PostMapping("/{articleId}")
    public ResponseEntity<Map<String, String>> addBookmark(
            @PathVariable Long articleId,
            Principal principal
    ) {
        User user = getCurrentUser(principal);
        bookmarkService.addBookmark(user, articleId);
        return ResponseEntity.ok(Map.of("message", "즐겨찾기가 추가되었습니다."));
    }

    /**
     * 즐겨찾기 삭제
     * @param articleId 기사 ID
     * @param principal 인증된 사용자 (이메일)
     * @return 성공 응답
     */
    @DeleteMapping("/{articleId}")
    public ResponseEntity<Map<String, String>> removeBookmark(
            @PathVariable Long articleId,
            Principal principal
    ) {
        User user = getCurrentUser(principal);
        bookmarkService.removeBookmark(user, articleId);
        return ResponseEntity.ok(Map.of("message", "즐겨찾기가 삭제되었습니다."));
    }

    /**
     * 내 즐겨찾기 목록 조회
     * @param principal 인증된 사용자 (이메일)
     * @param pageable 페이지 정보 (기본값: size=10, sort=createdDate,desc)
     * @return 즐겨찾기한 기사 목록
     */
    @GetMapping("/me")
    public ResponseEntity<Page<ArticleListResponseDto>> getMyBookmarks(
            Principal principal,
            @PageableDefault(size = 10, sort = "createdDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        User user = getCurrentUser(principal);
        Page<ArticleListResponseDto> bookmarks = bookmarkService.getBookmarks(user, pageable);
        return ResponseEntity.ok(bookmarks);
    }

    /**
     * 특정 기사의 즐겨찾기 여부 확인
     * @param articleId 기사 ID
     * @param principal 인증된 사용자 (이메일)
     * @return 즐겨찾기 여부
     */
    @GetMapping("/{articleId}/check")
    public ResponseEntity<Map<String, Boolean>> checkBookmark(
            @PathVariable Long articleId,
            Principal principal
    ) {
        User user = getCurrentUser(principal);
        boolean isBookmarked = bookmarkService.isBookmarked(user, articleId);
        return ResponseEntity.ok(Map.of("isBookmarked", isBookmarked));
    }

    /**
     * 내 즐겨찾기 개수 조회
     * @param principal 인증된 사용자 (이메일)
     * @return 즐겨찾기 개수
     */
    @GetMapping("/me/count")
    public ResponseEntity<Map<String, Long>> getMyBookmarkCount(
            Principal principal
    ) {
        User user = getCurrentUser(principal);
        long count = bookmarkService.getBookmarkCount(user);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
