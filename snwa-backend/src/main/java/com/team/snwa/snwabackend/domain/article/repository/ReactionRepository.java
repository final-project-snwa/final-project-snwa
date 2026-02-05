package com.team.snwa.snwabackend.domain.article.repository;

import com.team.snwa.snwabackend.domain.article.entity.Reaction;
import com.team.snwa.snwabackend.domain.article.entity.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    /**
     * 특정 사용자가 특정 기사에 남긴 반응 조회
     */
    Optional<Reaction> findByUserIdAndArticleId(Long userId, Long articleId);

    /**
     * 특정 사용자가 특정 기사에 반응했는지 확인
     */
    boolean existsByUserIdAndArticleId(Long userId, Long articleId);

    /**
     * 특정 기사의 특정 반응 타입 개수 조회
     */
    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.articleId = :articleId AND r.reactionType = :reactionType")
    long countByArticleIdAndReactionType(@Param("articleId") Long articleId, @Param("reactionType") ReactionType reactionType);

    /**
     * 특정 기사의 좋아요 개수
     */
    default long countLikes(Long articleId) {
        return countByArticleIdAndReactionType(articleId, ReactionType.LIKE);
    }

    /**
     * 특정 기사의 싫어요 개수
     */
    default long countDislikes(Long articleId) {
        return countByArticleIdAndReactionType(articleId, ReactionType.DISLIKE);
    }

    /**
     * 특정 기사의 슬퍼요 개수
     */
    default long countSads(Long articleId) {
        return countByArticleIdAndReactionType(articleId, ReactionType.SAD);
    }

    /**
     * 특정 기사의 화나요 개수
     */
    default long countAngries(Long articleId) {
        return countByArticleIdAndReactionType(articleId, ReactionType.ANGRY);
    }
}
