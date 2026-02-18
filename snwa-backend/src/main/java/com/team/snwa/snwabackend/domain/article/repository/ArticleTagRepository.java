package com.team.snwa.snwabackend.domain.article.repository;

import com.team.snwa.snwabackend.domain.article.entity.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {

    boolean existsByArticleId(Long articleId);

    boolean existsByArticleIdAndLanguage(Long articleId, String language);

    List<ArticleTag> findAllByArticleId(Long articleId);
}
