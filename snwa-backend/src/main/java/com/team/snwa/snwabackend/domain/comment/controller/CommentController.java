package com.team.snwa.snwabackend.domain.comment.controller;

import com.team.snwa.snwabackend.domain.comment.dto.request.CommentRequestDto;
import com.team.snwa.snwabackend.domain.comment.dto.response.CommentResponseDto;
import com.team.snwa.snwabackend.domain.comment.service.CommentService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    /**
     * 기사에 새로운 댓글을 작성함
     *
     * @param articleId  댓글을 달 기사의 ID
     * @param requestDto 댓글 내용
     * @param email      인증된 사용자의 이메일 (JWT 토큰에서 추출)
     * @return 생성된 댓글 정보
     *
     * @DateOfCreated 2026-02-03
     * @DateOfEdit 2026-02-03
     */
    @PostMapping("/articles/{articleId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long articleId,
            @RequestBody CommentRequestDto requestDto,
            @AuthenticationPrincipal String email
    ) {
        User user = findUserByEmail(email);
        return ResponseEntity.ok(commentService.createComment(articleId, requestDto, user));
    }

    /**
     * 기사의 댓글 목록을 페이징하여 조회함
     *
     * @param articleId 조회할 기사의 ID
     * @param pageable  페이징 설정 (기본값: 생성일 내림차순, 10개씩)
     * @return 댓글 목록 페이지
     *
     * @DateOfCreated 2026-02-03
     * @DateOfEdit 2026-02-03
     */
    @GetMapping("/articles/{articleId}/comments")
    public ResponseEntity<Page<CommentResponseDto>> getComments(
            @PathVariable Long articleId,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(commentService.getComments(articleId, pageable));
    }

    /**
     * 댓글 내용을 수정함
     *
     * @param commentId  수정할 댓글의 ID
     * @param requestDto 수정할 내용
     * @param email      인증된 사용자의 이메일
     * @return 수정된 댓글 정보
     *
     * @DateOfCreated 2026-02-03
     * @DateOfEdit 2026-02-03
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto requestDto,
            @AuthenticationPrincipal String email
    ) {
        User user = findUserByEmail(email);
        return ResponseEntity.ok(commentService.updateComment(commentId, requestDto, user));
    }

    /**
     * 댓글을 삭제함
     *
     * @param commentId 삭제할 댓글의 ID
     * @param email     인증된 사용자의 이메일
     * @return 응답 본문 없음 (204 No Content)
     *
     * @DateOfCreated 2026-02-03
     * @DateOfEdit 2026-02-03
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal String email
    ) {
        User user = findUserByEmail(email);
        commentService.deleteComment(commentId, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * 이메일로 사용자 엔티티를 조회함 (내부 헬퍼 메서드)
     *
     * @param email 조회할 사용자 이메일
     * @return 조회된 User 엔티티
     *
     * @DateOfCreated 2026-02-03
     * @DateOfEdit 2026-02-03
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}