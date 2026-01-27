package com.team.snwa.snwabackend.domain.article.repository;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("SELECT a FROM Article a " +
            "LEFT JOIN FETCH a.category " +
            "WHERE (:categoryId IS NULL OR a.category.id = :categoryId) " +
            "ORDER BY a.createdDate DESC")
    Page<Article> findAllWithCategory(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("SELECT a FROM Article a " +
            "LEFT JOIN FETCH a.category " +
            "WHERE a.id = :id")
    Optional<Article> findByIdWithCategory(@Param("id") Long id);

    /**
     * 번역된 제목과 내용에서 검색어를 포함하는 기사 검색
     * @param keyword 검색어
     * @param pageable 페이지 정보
     * @return 검색된 기사 목록
     */
    @Query("SELECT a FROM Article a " +
            "LEFT JOIN FETCH a.category " +
            "WHERE (LOWER(a.translatedTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.translatedContent) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY a.createdDate DESC")
    Page<Article> searchByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 번역된 제목에서만 검색어를 포함하는 기사 검색
     * @param keyword 검색어
     * @param pageable 페이지 정보
     * @return 검색된 기사 목록
     */
    @Query("SELECT a FROM Article a " +
            "LEFT JOIN FETCH a.category " +
            "WHERE LOWER(a.translatedTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY a.createdDate DESC")
    Page<Article> searchByTitle(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 번역된 내용에서만 검색어를 포함하는 기사 검색
     * @param keyword 검색어
     * @param pageable 페이지 정보
     * @return 검색된 기사 목록
     */
    @Query("SELECT a FROM Article a " +
            "LEFT JOIN FETCH a.category " +
            "WHERE LOWER(a.translatedContent) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY a.createdDate DESC")
    Page<Article> searchByContent(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 같은 카테고리의 최신 기사 조회 (현재 기사 제외)
     * @param categoryId 카테고리 ID
     * @param excludeArticleId 제외할 기사 ID
     * @param limit 조회할 개수
     * @return 관련 기사 목록
     */
    @Query("SELECT a FROM Article a " +
            "LEFT JOIN FETCH a.category " +
            "WHERE a.category.id = :categoryId " +
            "AND a.id != :excludeArticleId " +
            "ORDER BY a.createdDate DESC")
    java.util.List<Article> findRelatedArticles(
            @Param("categoryId") Long categoryId,
            @Param("excludeArticleId") Long excludeArticleId,
            org.springframework.data.domain.Pageable limit
    );

    // 크롤링한 기사 중복 검사용
    boolean existsByOriginalUrl(String originalUrl);
}