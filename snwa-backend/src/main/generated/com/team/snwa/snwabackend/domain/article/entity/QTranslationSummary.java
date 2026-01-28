package com.team.snwa.snwabackend.domain.article.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTranslationSummary is a Querydsl query type for TranslationSummary
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTranslationSummary extends EntityPathBase<TranslationSummary> {

    private static final long serialVersionUID = -160574910L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTranslationSummary translationSummary = new QTranslationSummary("translationSummary");

    public final QArticle article;

    public final QCategory category;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath text = createString("text");

    public QTranslationSummary(String variable) {
        this(TranslationSummary.class, forVariable(variable), INITS);
    }

    public QTranslationSummary(Path<? extends TranslationSummary> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTranslationSummary(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTranslationSummary(PathMetadata metadata, PathInits inits) {
        this(TranslationSummary.class, metadata, inits);
    }

    public QTranslationSummary(Class<? extends TranslationSummary> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.article = inits.isInitialized("article") ? new QArticle(forProperty("article"), inits.get("article")) : null;
        this.category = inits.isInitialized("category") ? new QCategory(forProperty("category")) : null;
    }

}

