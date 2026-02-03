package com.team.snwa.snwabackend.domain.user.controller;

import com.team.snwa.snwabackend.domain.article.dto.response.AdminArticleListResponse;
import com.team.snwa.snwabackend.domain.user.dto.request.AdminUserUpdateRequest;
import com.team.snwa.snwabackend.domain.user.dto.response.AdminUserResponse;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.domain.user.service.AdminService;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;

    /**
     * Principal에서 현재 사용자 정보 가져오기
     */
    private User getCurrentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 전체 회원 조회 (관리자 전용)
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers(Principal principal) {
        User currentUser = getCurrentUser(principal);
        List<AdminUserResponse> users = adminService.getAllUsers(currentUser);
        return ResponseEntity.ok(users);
    }

    /**
     * 관리자가 특정 사용자의 정보 수정
     * PATCH /api/admin/users/{userId}
     */
    @PatchMapping("/users/{userId}")
    public ResponseEntity<AdminUserResponse> updateUser(
            Principal principal,
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        User currentUser = getCurrentUser(principal);
        AdminUserResponse updatedUser = adminService.updateUser(currentUser, userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 관리자가 사용자 소프트 삭제 (로그인 불가능하게)
     * DELETE /api/admin/users/{userId}
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(
            Principal principal,
            @PathVariable Long userId) {
        User currentUser = getCurrentUser(principal);
        adminService.deleteUser(currentUser, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 전체 글 목록 조회 (관리자 전용)
     * GET /api/admin/articles
     */
    @GetMapping("/articles")
    public ResponseEntity<List<AdminArticleListResponse>> getAllArticles(Principal principal) {
        User currentUser = getCurrentUser(principal);
        List<AdminArticleListResponse> articles = adminService.getAllArticles(currentUser);
        return ResponseEntity.ok(articles);
    }

    /**
     * 관리자가 글 소프트 삭제 (다른 사람들에게 안 보이게)
     * DELETE /api/admin/articles/{articleId}
     */
    @DeleteMapping("/articles/{articleId}")
    public ResponseEntity<Void> deleteArticle(
            Principal principal,
            @PathVariable Long articleId) {
        User currentUser = getCurrentUser(principal);
        adminService.deleteArticle(currentUser, articleId);
        return ResponseEntity.ok().build();
    }
}
