package com.projectw.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserDslRepositoryImpl implements UserDslRepository {
    private final JPAQueryFactory jpaQueryFactory;

}
