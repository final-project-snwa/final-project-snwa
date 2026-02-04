package com.team.snwa.snwabackend.domain.interest.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "interest_target")
public class InterestTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestType type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String tagKey;

    @Builder
    public InterestTarget(InterestType type, String name, String tagKey) {
        this.type = type;
        this.name = name;
        this.tagKey = tagKey;
    }
}
