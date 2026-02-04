package com.team.snwa.snwabackend.domain.interest.repository;

import com.team.snwa.snwabackend.domain.interest.entity.InterestTarget;
import com.team.snwa.snwabackend.domain.interest.entity.UserSubscription;
import com.team.snwa.snwabackend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    List<UserSubscription> findByUser(User user);

    Optional<UserSubscription> findByUserAndInterestTarget(User user, InterestTarget interestTarget);

    boolean existsByUserAndInterestTarget(User user, InterestTarget interestTarget);

    void deleteByUserAndInterestTarget(User user, InterestTarget interestTarget);

    @Query("SELECT us.user FROM UserSubscription us WHERE us.interestTarget.tagKey = :tagKey AND us.isAlarmOn = true")
    List<User> findSubscribedUsersByTagKey(@Param("tagKey") String tagKey);

    @Query("SELECT us.user FROM UserSubscription us WHERE us.interestTarget.tagKey IN :tagKeys AND us.isAlarmOn = true")
    List<User> findSubscribedUsersByTagKeys(@Param("tagKeys") List<String> tagKeys);
}
