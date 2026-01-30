package com.team.snwa.snwabackend.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPasswordResetToken is a Querydsl query type for PasswordResetToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPasswordResetToken extends EntityPathBase<PasswordResetToken> {

    private static final long serialVersionUID = 200586067L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPasswordResetToken passwordResetToken = new QPasswordResetToken("passwordResetToken");

    public final DateTimePath<java.time.LocalDateTime> expiryDate = createDateTime("expiryDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath token = createString("token");

    public final BooleanPath used = createBoolean("used");

    public final QUser user;

    public QPasswordResetToken(String variable) {
        this(PasswordResetToken.class, forVariable(variable), INITS);
    }

    public QPasswordResetToken(Path<? extends PasswordResetToken> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPasswordResetToken(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPasswordResetToken(PathMetadata metadata, PathInits inits) {
        this(PasswordResetToken.class, metadata, inits);
    }

    public QPasswordResetToken(Class<? extends PasswordResetToken> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

