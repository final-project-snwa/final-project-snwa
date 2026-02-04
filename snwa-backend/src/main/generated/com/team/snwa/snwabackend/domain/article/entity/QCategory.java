package com.team.snwa.snwabackend.domain.article.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCategory is a Querydsl query type for Category
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCategory extends EntityPathBase<Category> {

    private static final long serialVersionUID = -847980597L;

    public static final QCategory category = new QCategory("category");

    public final EnumPath<com.team.snwa.snwabackend.domain.article.entity.enums.CategoryName> categoryName = createEnum("categoryName", com.team.snwa.snwabackend.domain.article.entity.enums.CategoryName.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QCategory(String variable) {
        super(Category.class, forVariable(variable));
    }

    public QCategory(Path<? extends Category> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCategory(PathMetadata metadata) {
        super(Category.class, metadata);
    }

}

