package com.team.snwa.snwabackend.domain.article.repository;

import com.team.snwa.snwabackend.domain.article.entity.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {

    boolean existsByArticleId(Long articleId);

    boolean existsByArticleIdAndLanguage(Long articleId, String language);

    List<ArticleTag> findByArticleIdAndLanguage(Long articleId, String language);

    List<ArticleTag> findAllByArticleId(Long articleId);

    @Query("SELECT DISTINCT at.tagName FROM ArticleTag at WHERE LOWER(at.tagName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<String> findDistinctTagNamesContaining(@Param("keyword") String keyword);
}
