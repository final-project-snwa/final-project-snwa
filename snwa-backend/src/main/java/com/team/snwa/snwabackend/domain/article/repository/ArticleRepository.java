package com.team.snwa.snwabackend.domain.article.repository;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

        @Query("SELECT a FROM Article a " +
                        "LEFT JOIN FETCH a.category " +
                        "WHERE a.deletedAt IS NULL " +
                        "AND (:categoryId IS NULL OR a.category.id = :categoryId) " +
                        "AND (:publisherName IS NULL OR :publisherName = '' OR a.publisherName = :publisherName) " +
                        "ORDER BY a.createdDate DESC")
        Page<Article> findAllWithCategory(
                        @Param("categoryId") Long categoryId,
                        @Param("publisherName") String publisherName,
                        Pageable pageable);

        @Query("SELECT a FROM Article a " +
                        "LEFT JOIN FETCH a.category " +
                        "WHERE a.id = :id AND a.deletedAt IS NULL")
        Optional<Article> findByIdWithCategory(@Param("id") Long id);

        /**
         * clickCount가 null일 경우 0으로 간주해서 1을 더하는 안전한 조회수 증가 쿼리
         * (DB에서 직접 증가하여 동시 요청 시 Lost Update 방지)
         * 
         * @param id 조회수를 증가시킬 Article 엔티티의 ID
         */
        @Modifying
        @Query("UPDATE Article a SET a.clickCount = COALESCE(a.clickCount, 0) + 1 WHERE a.id = :id")
        void incrementClickCountById(@Param("id") Long id);

        /**
         * 번역된 제목과 내용에서 검색어를 포함하는 기사 검색 (ArticleTranslation 조인)
         */
        @Query("SELECT a FROM Article a " +
                        "JOIN ArticleTranslation t ON t.article = a " +
                        "LEFT JOIN FETCH a.category " +
                        "WHERE a.deletedAt IS NULL " +
                        "AND t.language = 'KO' " +
                        "AND (LOWER(t.translatedTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(t.translatedContent) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "ORDER BY a.createdDate DESC")
        Page<Article> searchByKeyword(
                        @Param("keyword") String keyword,
                        Pageable pageable);

        /**
         * 번역된 제목에서만 검색어를 포함하는 기사 검색
         */
        @Query("SELECT a FROM Article a " +
                        "JOIN ArticleTranslation t ON t.article = a " +
                        "LEFT JOIN FETCH a.category " +
                        "WHERE a.deletedAt IS NULL " +
                        "AND t.language = 'KO' " +
                        "AND LOWER(t.translatedTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "ORDER BY a.createdDate DESC")
        Page<Article> searchByTitle(
                        @Param("keyword") String keyword,
                        Pageable pageable);

        /**
         * 번역된 내용에서만 검색어를 포함하는 기사 검색
         */
        @Query("SELECT a FROM Article a " +
                        "JOIN ArticleTranslation t ON t.article = a " +
                        "LEFT JOIN FETCH a.category " +
                        "WHERE a.deletedAt IS NULL " +
                        "AND t.language = 'KO' " +
                        "AND LOWER(t.translatedContent) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "ORDER BY a.createdDate DESC")
        Page<Article> searchByContent(
                        @Param("keyword") String keyword,
                        Pageable pageable);

        /**
         * 같은 카테고리의 최신 기사 조회 (현재 기사 제외)
         * 
         * @param categoryId       카테고리 ID
         * @param excludeArticleId 제외할 기사 ID
         * @param limit            조회할 개수
         * @return 관련 기사 목록
         */
        @Query("SELECT a FROM Article a " +
                        "LEFT JOIN FETCH a.category " +
                        "WHERE a.deletedAt IS NULL " +
                        "AND a.category.id = :categoryId " +
                        "AND a.id != :excludeArticleId " +
                        "ORDER BY a.createdDate DESC")
        List<Article> findRelatedArticles(
                        @Param("categoryId") Long categoryId,
                        @Param("excludeArticleId") Long excludeArticleId,
                        Pageable limit);

        /**
         * 관리자용: 삭제되지 않은 기사만 조회 (등록일 내림차순)
         */
        @Query("SELECT a FROM Article a LEFT JOIN FETCH a.user WHERE a.deletedAt IS NULL")
        List<Article> findAllByDeletedAtIsNull(Sort sort);

        // 크롤링한 기사 중복 검사용
        boolean existsByOriginalUrl(String originalUrl);

        Optional<Article> findByOriginalUrl(String originalUrl);

        /**
         * 삭제되지 않은 기사에서 출판사명 목록 (중복 제거, null/빈 문자열 제외)
         */
        @Query("SELECT DISTINCT a.publisherName FROM Article a WHERE a.deletedAt IS NULL AND a.publisherName IS NOT NULL AND a.publisherName != '' ORDER BY a.publisherName")
        List<String> findDistinctPublisherNames();

        /**
         * 번역이 안된 기사 조회 (ArticleTranslation에 KO 레코드가 없거나 내용이 없는 경우)
         */
        @Query(value = "SELECT a FROM Article a " +
                        "LEFT JOIN FETCH a.category " +
                        "WHERE a.deletedAt IS NULL " +
                        "AND NOT EXISTS (SELECT t FROM ArticleTranslation t WHERE t.article = a AND t.language = 'KO') "
                        +
                        "AND a.title IS NOT NULL " +
                        "AND a.content IS NOT NULL " +
                        "ORDER BY a.createdDate ASC", countQuery = "SELECT COUNT(a) FROM Article a " +
                                        "WHERE a.deletedAt IS NULL " +
                                        "AND NOT EXISTS (SELECT t FROM ArticleTranslation t WHERE t.article = a AND t.language = 'KO') "
                                        +
                                        "AND a.title IS NOT NULL AND a.content IS NOT NULL")
        Page<Article> findArticlesNeedingTranslation(Pageable pageable);

        /**
         * 요약이 안된 기사 조회 (ArticleTranslation은 있는데 summary가 없는 경우)
         */
        @Query(value = "SELECT a FROM Article a " +
                        "JOIN ArticleTranslation t ON t.article = a " +
                        "LEFT JOIN FETCH a.category " +
                        "WHERE a.deletedAt IS NULL " +
                        "AND t.language = 'KO' " +
                        "AND t.summary IS NULL " +
                        "AND t.translatedContent IS NOT NULL " +
                        "ORDER BY a.createdDate ASC", countQuery = "SELECT COUNT(a) FROM Article a " +
                                        "JOIN ArticleTranslation t ON t.article = a " +
                                        "WHERE a.deletedAt IS NULL " +
                                        "AND t.language = 'KO' " +
                                        "AND t.summary IS NULL " +
                                        "AND t.translatedContent IS NOT NULL")
        Page<Article> findArticlesNeedingSummary(Pageable pageable);

        /**
         * 키워드 추출이 필요한 기사 조회 (번역은 있는데 태그가 없는 경우)
         */
        @Query(value = "SELECT a FROM Article a " +
                        "JOIN ArticleTranslation t ON t.article = a " +
                        "LEFT JOIN FETCH a.category " +
                        "WHERE a.deletedAt IS NULL " +
                        "AND t.language = 'KO' " +
                        "AND t.translatedContent IS NOT NULL " +
                        "AND NOT EXISTS (SELECT 1 FROM ArticleTag at WHERE at.article.id = a.id AND at.language = 'KO') "
                        +
                        "ORDER BY a.createdDate ASC", countQuery = "SELECT COUNT(a) FROM Article a " +
                                        "JOIN ArticleTranslation t ON t.article = a " +
                                        "WHERE a.deletedAt IS NULL " +
                                        "AND t.language = 'KO' " +
                                        "AND t.translatedContent IS NOT NULL " +
                                        "AND NOT EXISTS (SELECT 1 FROM ArticleTag at WHERE at.article.id = a.id AND at.language = 'KO')")
        Page<Article> findArticlesNeedingKeywordExtraction(Pageable pageable);

        @Query("SELECT a.originalUrl FROM Article a WHERE a.originalUrl IN :urls")
        List<String> findExistingUrls(@Param("urls") List<String> urls);

        List<Article> findByOriginalUrlIn(List<String> originalUrls);

}