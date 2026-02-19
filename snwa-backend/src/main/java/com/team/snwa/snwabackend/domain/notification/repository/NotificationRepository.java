package com.team.snwa.snwabackend.domain.notification.repository;

import com.team.snwa.snwabackend.domain.notification.entity.Notification;
import com.team.snwa.snwabackend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    //유저 알림 목록 조회(최신순)
    @EntityGraph(attributePaths = "article")
    Page<Notification> findByUserOrderByCreatedDateDesc(
            User user,
            Pageable pageable
    );

    //읽지 않은 알림 목록
    @EntityGraph(attributePaths = "article")
    Page<Notification> findByUserAndIsReadFalseOrderByCreatedDateDesc(
            User user,
            Pageable pageable
    );

    //읽지 않은 알림 개수
    long countByUserAndIsReadFalse(User user);
}