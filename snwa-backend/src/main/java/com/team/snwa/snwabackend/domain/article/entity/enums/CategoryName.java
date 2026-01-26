package com.team.snwa.snwabackend.domain.article.entity.enums;

import lombok.Getter;

@Getter
public enum CategoryName {
    BASKETBALL(1),
    SOCCER(2),
    BASEBALL(3),
    FOOTBALL(4);

    private final int id;

    CategoryName(int id) {
        this.id = id;
    }

    /**
     * DB ID 값으로 Enum 상수를 찾아 반환함
     *
     * @param id 데이터베이스에 저장된 카테고리 ID
     * @return 매칭되는 CategoryName
     * @author 허준형
     * @DateOfCreated 2026-01-26
     */
    public static CategoryName fromId(int id) {
        for (CategoryName type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid Category ID: " + id);
    }
}