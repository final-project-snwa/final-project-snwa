package com.team.snwa.snwabackend.domain.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPaymentCancel is a Querydsl query type for PaymentCancel
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentCancel extends EntityPathBase<PaymentCancel> {

    private static final long serialVersionUID = -1213451677L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentCancel paymentCancel = new QPaymentCancel("paymentCancel");

    public final com.team.snwa.snwabackend.global.common.QBaseTimeEntity _super = new com.team.snwa.snwabackend.global.common.QBaseTimeEntity(this);

    public final NumberPath<Long> cancelAmount = createNumber("cancelAmount", Long.class);

    public final DateTimePath<java.time.LocalDateTime> canceledAt = createDateTime("canceledAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPayment payment;

    public final StringPath paymentKey = createString("paymentKey");

    public final StringPath rawJson = createString("rawJson");

    public final StringPath reason = createString("reason");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedDate = _super.updatedDate;

    public QPaymentCancel(String variable) {
        this(PaymentCancel.class, forVariable(variable), INITS);
    }

    public QPaymentCancel(Path<? extends PaymentCancel> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPaymentCancel(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPaymentCancel(PathMetadata metadata, PathInits inits) {
        this(PaymentCancel.class, metadata, inits);
    }

    public QPaymentCancel(Class<? extends PaymentCancel> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.payment = inits.isInitialized("payment") ? new QPayment(forProperty("payment")) : null;
    }

}

