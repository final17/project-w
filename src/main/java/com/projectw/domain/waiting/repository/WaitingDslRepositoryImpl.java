package com.projectw.domain.waiting.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WaitingDslRepositoryImpl implements WaitingDslRepository {
    private final JPAQueryFactory jpaQueryFactory;

}
