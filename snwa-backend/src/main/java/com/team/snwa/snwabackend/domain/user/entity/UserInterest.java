package com.team.snwa.snwabackend.domain.user.entity;

import com.team.snwa.snwabackend.domain.article.entity.Category;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "user_interests")
@Getter
public class UserInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 유저 참조

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // 카테고리 참조
}