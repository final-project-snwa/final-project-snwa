package com.team.snwa.snwabackend.domain.translation.repository;

import com.team.snwa.snwabackend.domain.translation.entity.TranslationAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TranslationAccessLogRepository extends JpaRepository<TranslationAccessLog, Long> {
    boolean existsByUserIdAndArticleIdAndLanguage(Long userId, Long articleId, String language);

    @Query("SELECT DISTINCT t.language FROM TranslationAccessLog t WHERE t.user.id = :userId AND t.article.id = :articleId")
    List<String> findDistinctLanguagesByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);
}
