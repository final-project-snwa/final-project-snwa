package com.team.snwa.snwabackend.domain.exp.repository;

import com.team.snwa.snwabackend.domain.exp.entity.UserExp;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserExpRepository extends JpaRepository<UserExp, Long> {
    Optional<UserExp> findByUserId(Long userId);

    List<UserExp> findByUserIdIn(Collection<Long> userIds);

    @Query("SELECT u FROM UserExp u WHERE u.status = 'ACTIVE' ORDER BY u.totalExp DESC, u.userId DESC")
    List<UserExp> findLeaderboardAll(Pageable pageable);
}
