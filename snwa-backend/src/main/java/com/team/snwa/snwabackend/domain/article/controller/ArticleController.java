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
