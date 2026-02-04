package com.team.snwa.snwabackend.domain.article.service;

import com.team.snwa.snwabackend.domain.article.entity.Like;
import com.team.snwa.snwabackend.domain.article.repository.LikeRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final LikeRepository likeRepository;

    /**
     * 기사 좋아요 생성
     */
    @Transactional
    public void addLike(Long userId, Long articleId) {
        if (likeRepository.existsByUserIdAndArticleId(userId, articleId)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        likeRepository.save(new Like(userId, articleId));
    }

    /**
     * 기사 좋아요 삭제
     */
    @Transactional
    public void removeLike(Long userId, Long articleId) {
        Like like = likeRepository.findByUserIdAndArticleId(userId, articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
        likeRepository.delete(like);
    }

    /**
     * 특정 기사 총 좋아요 수
     */
    public long getLikeCount(Long articleId) {
        return likeRepository.countByArticleId(articleId);
    }

    /**
     * 사용자가 좋아요를 눌렀는지 여부
     */
    public boolean isArticleLikedByUser(Long userId, Long articleId) {
        return likeRepository.existsByUserIdAndArticleId(userId, articleId);
    }
}
