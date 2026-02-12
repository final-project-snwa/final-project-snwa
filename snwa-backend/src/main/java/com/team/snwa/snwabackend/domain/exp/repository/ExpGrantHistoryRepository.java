package com.team.snwa.snwabackend.domain.exp.repository;

import com.team.snwa.snwabackend.domain.exp.entity.ExpGrantHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface ExpGrantHistoryRepository extends JpaRepository<ExpGrantHistory, Long> {
    boolean existsByUserIdAndGrantKey(Long userId, String grantKey);

    @Query("SELECT COUNT(e) FROM ExpGrantHistory e WHERE e.userId = :userId AND e.grantType = :grantType " +
            "AND FUNCTION('DATE', e.createdAt) = :date")
    long countByUserIdAndGrantTypeAndDate(Long userId, String grantType, LocalDate date);
}
