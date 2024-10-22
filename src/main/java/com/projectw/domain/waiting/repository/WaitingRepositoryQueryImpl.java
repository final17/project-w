package com.projectw.domain.waiting.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WaitingRepositoryQueryImpl implements WaitingRepositoryQuery {
    private final JPAQueryFactory jpaQueryFactory;

}
