package com.projectw.domain.reservation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReservationDslRepositoryImpl implements ReservationDslRepository{

    private final JPAQueryFactory queryFactory;
}
