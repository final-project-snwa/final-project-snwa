package com.team.snwa.snwabackend.domain.crawler.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCrawlingJob is a Querydsl query type for CrawlingJob
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCrawlingJob extends EntityPathBase<CrawlingJob> {

    private static final long serialVersionUID = 936399511L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCrawlingJob crawlingJob = new QCrawlingJob("crawlingJob");

    public final com.team.snwa.snwabackend.global.common.QBaseTimeEntity _super = new com.team.snwa.snwabackend.global.common.QBaseTimeEntity(this);

    public final com.team.snwa.snwabackend.domain.article.entity.QCategory category;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final StringPath cronExpression = createString("cronExpression");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath jobName = createString("jobName");

    public final DateTimePath<java.time.LocalDateTime> lastRunAt = createDateTime("lastRunAt", java.time.LocalDateTime.class);

    public final EnumPath<com.team.snwa.snwabackend.domain.crawler.entity.enums.SourceName> sourceName = createEnum("sourceName", com.team.snwa.snwabackend.domain.crawler.entity.enums.SourceName.class);

    public final StringPath targetUrl = createString("targetUrl");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedDate = _super.updatedDate;

    public QCrawlingJob(String variable) {
        this(CrawlingJob.class, forVariable(variable), INITS);
    }

    public QCrawlingJob(Path<? extends CrawlingJob> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCrawlingJob(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCrawlingJob(PathMetadata metadata, PathInits inits) {
        this(CrawlingJob.class, metadata, inits);
    }

    public QCrawlingJob(Class<? extends CrawlingJob> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.team.snwa.snwabackend.domain.article.entity.QCategory(forProperty("category")) : null;
    }

}

