package com.team.snwa.snwabackend.domain.article.repository;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("SELECT a FROM Article a " +
           "LEFT JOIN FETCH a.category " +
           "LEFT JOIN FETCH a.author " +
           "WHERE (:categoryId IS NULL OR a.category.id = :categoryId) " +
           "ORDER BY a.createdDate DESC")
    Page<Article> findAllWithCategoryAndAuthor(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("SELECT a FROM Article a " +
           "LEFT JOIN FETCH a.category " +
           "LEFT JOIN FETCH a.author " +
           "WHERE a.id = :id")
    Optional<Article> findByIdWithCategoryAndAuthor(@Param("id") Long id);
}
