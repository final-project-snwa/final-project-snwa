package com.team.snwa.snwabackend.domain.interest.repository;

import com.team.snwa.snwabackend.domain.interest.entity.InterestTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestTargetRepository extends JpaRepository<InterestTarget, Long> {
    Optional<InterestTarget> findByTagKey(String tagKey);

    List<InterestTarget> findByNameContaining(String keyword);

    List<InterestTarget> findByTagKeyIn(List<String> tagKeys);
}
