package com.team.snwa.snwabackend.domain.article.controller;

import com.team.snwa.snwabackend.domain.article.dto.ArticleDetailResponseDto;
import com.team.snwa.snwabackend.domain.article.dto.ArticleListResponseDto;
import com.team.snwa.snwabackend.domain.article.entity.Category;
import com.team.snwa.snwabackend.domain.article.dto.request.ArticleCreateRequestDto;
import com.team.snwa.snwabackend.domain.article.service.ArticleService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final UserRepository userRepository;

    /** Principal(이메일)이 있으면 User 조회, 없으면 null — 카테고리별 클릭(ClickLog)용 */
    private User resolveUser(Principal principal) {
        if (principal == null || principal.getName() == null) return null;
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }

    /**
     * 기사 생성
     * @param request 생성 요청 (categoryId, title, content 필수)
     * @param user 인증된 사용자 (글 작성자)
     * @return 생성된 기사 상세 정보
     */
    @PostMapping
    public ResponseEntity<ArticleDetailResponseDto> createArticle(
            @Valid @RequestBody ArticleCreateRequestDto request,
            @AuthenticationPrincipal User user
    ) {
        ArticleDetailResponseDto created = articleService.createArticle(user, request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(created);
    }

    /**
     * 기사 목록 조회
     * @param categoryId 카테고리 ID (선택적)
     * @param publisherName 출판사명 (선택적)
     * @param pageable 페이지 정보 (기본값: size=10, sort=createdDate,desc)
     * @return 기사 목록
     */
    @GetMapping
    public ResponseEntity<Page<ArticleListResponseDto>> getArticleList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String publisherName,
            @PageableDefault(size = 10, sort = "createdDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        Page<ArticleListResponseDto> articles = articleService.getArticleList(categoryId, publisherName, pageable, user);
        return ResponseEntity.ok(articles);
    }

    /**
     * 카테고리(종목) 목록 조회
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(articleService.getCategories());
    }

    /**
     * 출판사 목록 조회 (기사에 등장하는 출판사명)
     */
    @GetMapping("/publishers")
    public ResponseEntity<List<String>> getPublishers() {
        return ResponseEntity.ok(articleService.getPublisherNames());
    }

    /**
     * 기사 상세 조회
     * @param id 기사 ID
     * @return 기사 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDetailResponseDto> getArticleDetail(
            @PathVariable Long id,
            Principal principal,
            @RequestParam(required = false, defaultValue = "true") boolean recordView
    ) {
        User user = resolveUser(principal);
        ArticleDetailResponseDto article = articleService.getArticleDetail(id, user, recordView);
        return ResponseEntity.ok(article);
    }

    /**
     * 관련 기사 조회 (같은 카테고리의 최신 기사 3개, 현재 기사 제외)
     * @param id 현재 기사 ID
     * @return 관련 기사 목록 (최대 3개)
     */
    @GetMapping("/{id}/related")
    public ResponseEntity<List<ArticleListResponseDto>> getRelatedArticles(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        List<ArticleListResponseDto> relatedArticles = articleService.getRelatedArticles(id, user);
        return ResponseEntity.ok(relatedArticles);
    }

    /**
     * 기사 검색 (제목 + 내용)
     * @param keyword 검색어
     * @param pageable 페이지 정보 (기본값: size=10, sort=createdDate,desc)
     * @return 검색된 기사 목록
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ArticleListResponseDto>> searchArticles(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "createdDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        Page<ArticleListResponseDto> articles = articleService.searchArticles(keyword, pageable, user);
        return ResponseEntity.ok(articles);
    }

    /**
     * 제목만 검색
     * @param keyword 검색어
     * @param pageable 페이지 정보 (기본값: size=10, sort=createdDate,desc)
     * @return 검색된 기사 목록
     */
    @GetMapping("/search/title")
    public ResponseEntity<Page<ArticleListResponseDto>> searchByTitle(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "createdDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        Page<ArticleListResponseDto> articles = articleService.searchByTitle(keyword, pageable, user);
        return ResponseEntity.ok(articles);
    }

    /**
     * 내용만 검색
     * @param keyword 검색어
     * @param pageable 페이지 정보 (기본값: size=10, sort=createdDate,desc)
     * @return 검색된 기사 목록
     */
    @GetMapping("/search/content")
    public ResponseEntity<Page<ArticleListResponseDto>> searchByContent(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "createdDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        Page<ArticleListResponseDto> articles = articleService.searchByContent(keyword, pageable, user);
        return ResponseEntity.ok(articles);
    }

    /**
     * 기사 삭제
     * @param id 기사 ID
     * @return 삭제 성공 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }
}
