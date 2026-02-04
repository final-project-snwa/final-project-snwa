package com.team.snwa.snwabackend.domain.article.repository;

import com.team.snwa.snwabackend.domain.article.entity.ClickLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickLogRepository extends JpaRepository<ClickLog, Long> {

    @Query("SELECT c.categoryName, COUNT(cl) FROM ClickLog cl " +
            "JOIN cl.article a JOIN a.category c " +
            "WHERE cl.user.id = :userId GROUP BY c.categoryName")
    List<Object[]> countByUserIdGroupByCategory(@Param("userId") Long userId);
}
