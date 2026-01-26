package com.team.snwa.snwabackend.domain.article.repository;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("SELECT a FROM Article a " +
           "LEFT JOIN FETCH a.category " +
           "WHERE (:categoryId IS NULL OR a.category.id = :categoryId) " +
           "ORDER BY a.createdDate DESC")
    Page<Article> findAllWithCategory(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("SELECT a FROM Article a " +
           "LEFT JOIN FETCH a.category " +
           "WHERE a.id = :id")
    Optional<Article> findByIdWithCategory(@Param("id") Long id);
}
