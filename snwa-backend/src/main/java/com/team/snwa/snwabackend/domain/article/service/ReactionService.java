package com.team.snwa.snwabackend.domain.article.service;

import com.team.snwa.snwabackend.domain.article.dto.response.ReactionCountResponseDto;
import com.team.snwa.snwabackend.domain.article.entity.Reaction;
import com.team.snwa.snwabackend.domain.article.entity.enums.ReactionType;
import com.team.snwa.snwabackend.domain.article.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 기사 감정 반응 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReactionService {

    private final ReactionRepository reactionRepository;

    /**
     * 반응 추가 또는 변경
     * - 기존 반응이 없으면 새로 생성
     * - 기존 반응이 있고 같은 타입이면 취소 (삭제)
     * - 기존 반응이 있고 다른 타입이면 변경
     *
     * @return 최종 반응 타입 (취소된 경우 null)
     */
    @Transactional
    public ReactionType toggleReaction(Long userId, Long articleId, ReactionType reactionType) {
        Optional<Reaction> existingReaction = reactionRepository.findByUserIdAndArticleId(userId, articleId);

        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();
            if (reaction.getReactionType() == reactionType) {
                // 같은 반응 클릭 -> 취소
                reactionRepository.delete(reaction);
                return null;
            } else {
                // 다른 반응 클릭 -> 변경
                reaction.changeReactionType(reactionType);
                return reactionType;
            }
        } else {
            // 새로운 반응 추가
            reactionRepository.save(new Reaction(userId, articleId, reactionType));
            return reactionType;
        }
    }

    /**
     * 반응 삭제 (취소)
     */
    @Transactional
    public void removeReaction(Long userId, Long articleId) {
        reactionRepository.findByUserIdAndArticleId(userId, articleId)
                .ifPresent(reactionRepository::delete);
    }

    /**
     * 기사의 반응 개수 조회
     */
    public ReactionCountResponseDto getReactionCounts(Long articleId, Long userId) {
        long likeCount = reactionRepository.countLikes(articleId);
        long dislikeCount = reactionRepository.countDislikes(articleId);
        long sadCount = reactionRepository.countSads(articleId);
        long angryCount = reactionRepository.countAngries(articleId);

        ReactionType userReaction = null;
        if (userId != null) {
            userReaction = reactionRepository.findByUserIdAndArticleId(userId, articleId)
                    .map(Reaction::getReactionType)
                    .orElse(null);
        }

        return ReactionCountResponseDto.of(likeCount, dislikeCount, sadCount, angryCount, userReaction);
    }

    /**
     * 사용자의 반응 타입 조회
     */
    public ReactionType getUserReaction(Long userId, Long articleId) {
        return reactionRepository.findByUserIdAndArticleId(userId, articleId)
                .map(Reaction::getReactionType)
                .orElse(null);
    }
}
