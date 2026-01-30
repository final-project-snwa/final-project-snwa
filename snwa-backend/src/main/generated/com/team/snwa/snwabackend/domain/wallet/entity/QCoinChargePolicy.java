package com.team.snwa.snwabackend.domain.wallet.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCoinChargePolicy is a Querydsl query type for CoinChargePolicy
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoinChargePolicy extends EntityPathBase<CoinChargePolicy> {

    private static final long serialVersionUID = -1490660653L;

    public static final QCoinChargePolicy coinChargePolicy = new QCoinChargePolicy("coinChargePolicy");

    public final BooleanPath active = createBoolean("active");

    public final NumberPath<Integer> coinAmount = createNumber("coinAmount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public QCoinChargePolicy(String variable) {
        super(CoinChargePolicy.class, forVariable(variable));
    }

    public QCoinChargePolicy(Path<? extends CoinChargePolicy> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCoinChargePolicy(PathMetadata metadata) {
        super(CoinChargePolicy.class, metadata);
    }

}

