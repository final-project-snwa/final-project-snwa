package com.team.snwa.snwabackend.domain.article.repository;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.entity.Bookmark;
import com.team.snwa.snwabackend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    /**
     * 특정 사용자와 기사의 즐겨찾기 조회
     * @param user 사용자
     * @param article 기사
     * @return 즐겨찾기 (없으면 Optional.empty())
     */
    Optional<Bookmark> findByUserAndArticle(User user, Article article);

    /**
     * 특정 사용자의 모든 즐겨찾기 조회 (기사 정보 포함)
     * @param user 사용자
     * @param pageable 페이지 정보
     * @return 즐겨찾기 목록
     */
    @Query("SELECT b FROM Bookmark b " +
            "LEFT JOIN FETCH b.article a " +
            "LEFT JOIN FETCH a.category " +
            "WHERE b.user = :user AND a.deletedAt IS NULL " +
            "ORDER BY b.createdDate DESC")
    Page<Bookmark> findByUserWithArticle(@Param("user") User user, Pageable pageable);

    /**
     * 특정 사용자와 기사의 즐겨찾기 존재 여부 확인
     * @param user 사용자
     * @param article 기사
     * @return 존재 여부
     */
    boolean existsByUserAndArticle(User user, Article article);

    /**
     * 특정 사용자와 기사의 즐겨찾기 삭제
     * @param user 사용자
     * @param article 기사
     */
    void deleteByUserAndArticle(User user, Article article);

    /**
     * 특정 사용자의 즐겨찾기 개수 조회
     * @param user 사용자
     * @return 즐겨찾기 개수
     */
    long countByUser(User user);

    /**
     * 특정 사용자가 즐겨찾기한 기사 ID 목록 조회 (배치용)
     * @param user 사용자
     * @param articleIds 기사 ID 목록
     * @return 즐겨찾기한 기사 ID 목록
     */
    @Query("SELECT b.article.id FROM Bookmark b WHERE b.user = :user AND b.article.id IN :articleIds")
    List<Long> findArticleIdsByUserAndArticleIdIn(
            @Param("user") User user,
            @Param("articleIds") List<Long> articleIds
    );
}
