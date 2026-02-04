package com.team.snwa.snwabackend.domain.crawler.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QArticleCrawlingTracking is a Querydsl query type for ArticleCrawlingTracking
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QArticleCrawlingTracking extends EntityPathBase<ArticleCrawlingTracking> {

    private static final long serialVersionUID = 1540989693L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QArticleCrawlingTracking articleCrawlingTracking = new QArticleCrawlingTracking("articleCrawlingTracking");

    public final com.team.snwa.snwabackend.domain.article.entity.QArticle article;

    public final StringPath articleUrl = createString("articleUrl");

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final StringPath contentKo = createString("contentKo");

    public final StringPath contentOrigin = createString("contentOrigin");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> jobId = createNumber("jobId", Long.class);

    public final StringPath titleKo = createString("titleKo");

    public final StringPath titleOrigin = createString("titleOrigin");

    public QArticleCrawlingTracking(String variable) {
        this(ArticleCrawlingTracking.class, forVariable(variable), INITS);
    }

    public QArticleCrawlingTracking(Path<? extends ArticleCrawlingTracking> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QArticleCrawlingTracking(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QArticleCrawlingTracking(PathMetadata metadata, PathInits inits) {
        this(ArticleCrawlingTracking.class, metadata, inits);
    }

    public QArticleCrawlingTracking(Class<? extends ArticleCrawlingTracking> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.article = inits.isInitialized("article") ? new com.team.snwa.snwabackend.domain.article.entity.QArticle(forProperty("article"), inits.get("article")) : null;
    }

}

