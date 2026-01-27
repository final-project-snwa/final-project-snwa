package com.team.snwa.snwabackend.domain.article.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.team.snwa.snwabackend.domain.article.entity.enums.CategoryName;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "category")
@Getter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, length = 20)
    private CategoryName categoryName;
}