package com.team.snwa.snwabackend.domain.wallet.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCoinTransaction is a Querydsl query type for CoinTransaction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoinTransaction extends EntityPathBase<CoinTransaction> {

    private static final long serialVersionUID = -1685590159L;

    public static final QCoinTransaction coinTransaction = new QCoinTransaction("coinTransaction");

    public final com.team.snwa.snwabackend.global.common.QBaseTimeEntity _super = new com.team.snwa.snwabackend.global.common.QBaseTimeEntity(this);

    public final NumberPath<Long> amount = createNumber("amount", Long.class);

    public final NumberPath<Long> balanceAfter = createNumber("balanceAfter", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final StringPath externalRef = createString("externalRef");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionStatus> status = createEnum("status", com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionStatus.class);

    public final EnumPath<com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionType> type = createEnum("type", com.team.snwa.snwabackend.domain.wallet.entity.enums.CoinTransactionType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedDate = _super.updatedDate;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QCoinTransaction(String variable) {
        super(CoinTransaction.class, forVariable(variable));
    }

    public QCoinTransaction(Path<? extends CoinTransaction> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCoinTransaction(PathMetadata metadata) {
        super(CoinTransaction.class, metadata);
    }

}

