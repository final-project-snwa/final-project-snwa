package com.team.snwa.snwabackend.domain.article.repository;

import com.team.snwa.snwabackend.domain.article.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUserIdAndArticleId(Long userId, Long articleId);

    Optional<Like> findByUserIdAndArticleId(Long userId, Long articleId);

    long countByArticleId(Long articleId);

    void deleteByUserIdAndArticleId(Long userId, Long articleId);
}
