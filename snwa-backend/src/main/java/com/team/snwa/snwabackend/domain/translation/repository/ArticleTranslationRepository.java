package com.team.snwa.snwabackend.domain.translation.repository;

import com.team.snwa.snwabackend.domain.translation.entity.ArticleTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleTranslationRepository extends JpaRepository<ArticleTranslation, Long> {
    Optional<ArticleTranslation> findByArticleIdAndLanguage(Long articleId, String language);

    List<ArticleTranslation> findAllByArticleId(Long articleId);
}
