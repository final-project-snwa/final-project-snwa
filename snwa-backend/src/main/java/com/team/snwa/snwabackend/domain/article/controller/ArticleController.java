package com.team.snwa.snwabackend.domain.article.controller;

import com.team.snwa.snwabackend.domain.article.dto.ArticleDetailResponseDto;
import com.team.snwa.snwabackend.domain.article.dto.ArticleListResponseDto;
import com.team.snwa.snwabackend.domain.article.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 기사 목록 조회
     * @param categoryId 카테고리 ID (선택적)
     * @param pageable 페이지 정보 (기본값: size=10, sort=createdDate,desc)
     * @return 기사 목록
     */
    @GetMapping
    public ResponseEntity<Page<ArticleListResponseDto>> getArticleList(
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10, sort = "createdDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ArticleListResponseDto> articles = articleService.getArticleList(categoryId, pageable);
        return ResponseEntity.ok(articles);
    }

    /**
     * 기사 상세 조회
     * @param id 기사 ID
     * @return 기사 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDetailResponseDto> getArticleDetail(@PathVariable Long id) {
        ArticleDetailResponseDto article = articleService.getArticleDetail(id);
        return ResponseEntity.ok(article);
    }

    /**
     * 관련 기사 조회 (같은 카테고리의 최신 기사 3개, 현재 기사 제외)
     * @param id 현재 기사 ID
     * @return 관련 기사 목록 (최대 3개)
     */
    @GetMapping("/{id}/related")
    public ResponseEntity<List<ArticleListResponseDto>> getRelatedArticles(@PathVariable Long id) {
        List<ArticleListResponseDto> relatedArticles = articleService.getRelatedArticles(id);
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
            @PageableDefault(size = 10, sort = "createdDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ArticleListResponseDto> articles = articleService.searchArticles(keyword, pageable);
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
            @PageableDefault(size = 10, sort = "createdDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ArticleListResponseDto> articles = articleService.searchByTitle(keyword, pageable);
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
            @PageableDefault(size = 10, sort = "createdDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ArticleListResponseDto> articles = articleService.searchByContent(keyword, pageable);
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
