package com.team.snwa.snwabackend.domain.article.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClickLog is a Querydsl query type for ClickLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClickLog extends EntityPathBase<ClickLog> {

    private static final long serialVersionUID = 7943017L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClickLog clickLog = new QClickLog("clickLog");

    public final com.team.snwa.snwabackend.global.common.QBaseTimeEntity _super = new com.team.snwa.snwabackend.global.common.QBaseTimeEntity(this);

    public final QArticle article;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedDate = _super.updatedDate;

    public final com.team.snwa.snwabackend.domain.user.entity.QUser user;

    public QClickLog(String variable) {
        this(ClickLog.class, forVariable(variable), INITS);
    }

    public QClickLog(Path<? extends ClickLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClickLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClickLog(PathMetadata metadata, PathInits inits) {
        this(ClickLog.class, metadata, inits);
    }

    public QClickLog(Class<? extends ClickLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.article = inits.isInitialized("article") ? new QArticle(forProperty("article"), inits.get("article")) : null;
        this.user = inits.isInitialized("user") ? new com.team.snwa.snwabackend.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

