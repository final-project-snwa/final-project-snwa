package com.team.snwa.snwabackend.domain.article.service;

import com.team.snwa.snwabackend.domain.article.dto.ArticleDetailResponseDto;
import com.team.snwa.snwabackend.domain.article.dto.ArticleListResponseDto;
import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;

    /**
     * 기사 목록 조회
     * @param categoryId 카테고리 ID (선택적, null이면 전체 조회)
     * @param pageable 페이지 정보
     * @return 기사 목록
     */
    public Page<ArticleListResponseDto> getArticleList(Long categoryId, Pageable pageable) {
        Page<Article> articles = articleRepository.findAllWithCategory(categoryId, pageable);
        return articles.map(ArticleListResponseDto::from);
    }

    /**
     * 기사 상세 조회
     * @param id 기사 ID
     * @return 기사 상세 정보
     * @throws CustomException 기사를 찾을 수 없을 경우
     */
    public ArticleDetailResponseDto getArticleDetail(Long id) {
        Article article = articleRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));
        return ArticleDetailResponseDto.from(article);
    }

    /**
     * 기사 삭제
     * @param id 기사 ID
     * @throws CustomException 기사를 찾을 수 없을 경우
     */
    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));
        articleRepository.delete(article);
    }

    /**
     * 기사 검색 (제목 + 내용)
     * @param keyword 검색어
     * @param pageable 페이지 정보
     * @return 검색된 기사 목록
     */
    public Page<ArticleListResponseDto> searchArticles(String keyword, Pageable pageable) {
        Page<Article> articles = articleRepository.searchByKeyword(keyword, pageable);
        return articles.map(ArticleListResponseDto::from);
    }

    /**
     * 제목만 검색
     * @param keyword 검색어
     * @param pageable 페이지 정보
     * @return 검색된 기사 목록
     */
    public Page<ArticleListResponseDto> searchByTitle(String keyword, Pageable pageable) {
        Page<Article> articles = articleRepository.searchByTitle(keyword, pageable);
        return articles.map(ArticleListResponseDto::from);
    }

    /**
     * 내용만 검색
     * @param keyword 검색어
     * @param pageable 페이지 정보
     * @return 검색된 기사 목록
     */
    public Page<ArticleListResponseDto> searchByContent(String keyword, Pageable pageable) {
        Page<Article> articles = articleRepository.searchByContent(keyword, pageable);
        return articles.map(ArticleListResponseDto::from);
    }

    /**
     * 관련 기사 조회 (같은 카테고리의 최신 기사 3개, 현재 기사 제외)
     * @param articleId 현재 기사 ID
     * @return 관련 기사 목록 (최대 3개)
     */
    public List<ArticleListResponseDto> getRelatedArticles(Long articleId) {
        Article article = articleRepository.findByIdWithCategory(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));

        // 카테고리가 없는 경우 빈 리스트 반환
        if (article.getCategory() == null) {
            return List.of();
        }

        Long categoryId = article.getCategory().getId();
        Pageable pageable = PageRequest.of(0, 3);
        List<Article> relatedArticles = articleRepository.findRelatedArticles(
                categoryId,
                articleId,
                pageable
        );

        return relatedArticles.stream()
                .map(ArticleListResponseDto::from)
                .collect(Collectors.toList());
    }
}
