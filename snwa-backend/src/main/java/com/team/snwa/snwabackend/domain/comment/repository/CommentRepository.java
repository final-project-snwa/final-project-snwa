package com.team.snwa.snwabackend.domain.comment.repository;

import com.team.snwa.snwabackend.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 사용자가 작성한 댓글 목록을 생성일 내림차순으로 조회함
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.article WHERE c.user.id = :userId ORDER BY c.createdDate DESC")
    List<Comment> findByUserIdOrderByCreatedDateDesc(@Param("userId") Long userId);

    /**
     * 특정 기사에 달린 댓글 목록을 페이징하여 조회함
     * N+1 문제를 방지하기 위해 User 엔티티를 Fetch Join으로 함께 가져옴
     *
     * @param articleId 조회할 기사의 식별자
     * @param pageable  페이징 정보 (페이지 번호, 크기, 정렬 등)
     * @return 해당 기사의 댓글 페이지 객체
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.article.id = :articleId ORDER BY c.createdDate DESC")
    Page<Comment> findByArticleId(@Param("articleId") Long articleId, Pageable pageable);
}