package com.team.snwa.snwabackend.domain.translation.repository;

import com.team.snwa.snwabackend.domain.translation.entity.ArticleTranslation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleTranslationRepository extends JpaRepository<ArticleTranslation, Long> {

        /**
         * 특정 기사 ID와 언어에 해당하는 번역본을 조회합
         */
        Optional<ArticleTranslation> findByArticleIdAndLanguage(Long articleId, String language);

        /**
         * 특정 기사 ID의 모든 번역본 목록을 조회
         */
        List<ArticleTranslation> findAllByArticleId(Long articleId);

        /**
         * 여러 기사 ID와 특정 언어에 해당하는 번역본 목록을 조회
         */
        List<ArticleTranslation> findAllByArticleIdInAndLanguage(List<Long> articleIds, String language);

        /**
         * 특정 언어에서 제목 또는 내용에 키워드가 포함된 번역본을 검색 (기사 생성일 기준 내림차순)
         */
        @Query("SELECT t FROM ArticleTranslation t JOIN FETCH t.article a " +
                        "WHERE t.language = :language " +
                        "AND (LOWER(t.translatedTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(t.translatedContent) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "ORDER BY a.createdDate DESC")
        Page<ArticleTranslation> searchByKeyword(
                        @Param("keyword") String keyword,
                        @Param("language") String language,
                        Pageable pageable);

        /**
         * 특정 언어에서 제목에 키워드가 포함된 번역본을 검색 (기사 생성일 기준 내림차순)
         */
        @Query("SELECT t FROM ArticleTranslation t JOIN FETCH t.article a " +
                        "WHERE t.language = :language " +
                        "AND LOWER(t.translatedTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "ORDER BY a.createdDate DESC")
        Page<ArticleTranslation> searchByTitle(
                        @Param("keyword") String keyword,
                        @Param("language") String language,
                        Pageable pageable);

        /**
         * 특정 언어에서 내용에 키워드가 포함된 번역본을 검색 (기사 생성일 기준 내림차순)
         */
        @Query("SELECT t FROM ArticleTranslation t JOIN FETCH t.article a " +
                        "WHERE t.language = :language " +
                        "AND LOWER(t.translatedContent) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "ORDER BY a.createdDate DESC")
        Page<ArticleTranslation> searchByContent(
                        @Param("keyword") String keyword,
                        @Param("language") String language,
                        Pageable pageable);

        /**
         * 요약 정보가 아직 생성되지 않았고, 번역 내용이 존재하는 번역본 목록을 조회 (요약 스케줄러용)
         */
        @Query("SELECT t FROM ArticleTranslation t JOIN FETCH t.article a " +
                        "WHERE t.language = :language " +
                        "AND t.summary IS NULL " +
                        "AND t.translatedContent IS NOT NULL " +
                        "AND t.translatedContent != '' " +
                        "ORDER BY a.createdDate ASC")
        Page<ArticleTranslation> findTranslationsNeedingSummary(
                        @Param("language") String language,
                        Pageable pageable);

        /**
         * 번역 내용은 있지만 키워드(태그)가 추출되지 않은 번역본 목록을 조회 (키워드 추출용)
         */
        @Query("SELECT t FROM ArticleTranslation t JOIN FETCH t.article a " +
                        "WHERE t.language = :language " +
                        "AND t.translatedContent IS NOT NULL " +
                        "AND t.translatedContent != '' " +
                        "AND a.deletedAt IS NULL " +
                        "AND NOT EXISTS (SELECT 1 FROM ArticleTag at WHERE at.article.id = a.id AND at.language = :language) "
                        +
                        "ORDER BY a.createdDate ASC")
        Page<ArticleTranslation> findTranslationsNeedingKeywordExtraction(
                        @Param("language") String language,
                        Pageable pageable);

        /**
         * 특정 기사 ID와 언어의 번역본이 존재하는지 확인
         */
        boolean existsByArticleIdAndLanguage(Long articleId, String language);
}
