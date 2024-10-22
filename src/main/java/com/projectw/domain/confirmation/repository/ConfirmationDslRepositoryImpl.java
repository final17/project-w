package com.projectw.domain.confirmation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConfirmationDslRepositoryImpl implements ConfirmationDslRepository {
    private final JPAQueryFactory queryFactory;
}
