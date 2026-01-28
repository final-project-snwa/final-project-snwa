package com.team.snwa.snwabackend.domain.crawler.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCrawlingLog is a Querydsl query type for CrawlingLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCrawlingLog extends EntityPathBase<CrawlingLog> {

    private static final long serialVersionUID = 936401438L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCrawlingLog crawlingLog = new QCrawlingLog("crawlingLog");

    public final com.team.snwa.snwabackend.global.common.QBaseTimeEntity _super = new com.team.snwa.snwabackend.global.common.QBaseTimeEntity(this);

    public final NumberPath<Integer> collectedCount = createNumber("collectedCount", Integer.class);

    public final QCrawlingJob crawlingJob;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final DateTimePath<java.time.LocalDateTime> endTime = createDateTime("endTime", java.time.LocalDateTime.class);

    public final StringPath errorMessage = createString("errorMessage");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> startTime = createDateTime("startTime", java.time.LocalDateTime.class);

    public final EnumPath<com.team.snwa.snwabackend.domain.crawler.entity.enums.CrawlingStatus> status = createEnum("status", com.team.snwa.snwabackend.domain.crawler.entity.enums.CrawlingStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedDate = _super.updatedDate;

    public QCrawlingLog(String variable) {
        this(CrawlingLog.class, forVariable(variable), INITS);
    }

    public QCrawlingLog(Path<? extends CrawlingLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCrawlingLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCrawlingLog(PathMetadata metadata, PathInits inits) {
        this(CrawlingLog.class, metadata, inits);
    }

    public QCrawlingLog(Class<? extends CrawlingLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.crawlingJob = inits.isInitialized("crawlingJob") ? new QCrawlingJob(forProperty("crawlingJob"), inits.get("crawlingJob")) : null;
    }

}

