package com.projectw.domain.reservation.repository;


import static com.projectw.domain.reservation.entity.QReservation.reservation;
import static com.projectw.domain.store.entity.QStore.store;
import static com.projectw.domain.user.entity.QUser.user;

import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.entity.QReservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ReservationDslRepositoryImpl implements ReservationDslRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ReserveResponse.Infos> getOwnerReservations(Long userId, ReserveRequest.Parameter parameter, Pageable pageable) {

        List<ReserveResponse.Infos> results = queryFactory
                .select(Projections.constructor(ReserveResponse.Infos.class ,
                        user.id.as("userId") ,
                        store.id.as("storeId") ,
                        reservation.id.as("reservationId") ,
                        reservation.reservationNo ,
                        reservation.numberPeople ,
                        reservation.reservationDate ,
                        reservation.reservationTime ,
                        reservation.type ,
                        reservation.status))
                .from(user)
                .innerJoin(store).on(store.user.id.eq(user.id))
                .innerJoin(reservation).on(reservation.store.id.eq(store.id))
                .where(
                        user.id.eq(userId),
                        typeEquals(parameter.type()),
                        statusEquals(parameter.status()),
                        startDtEquals(parameter.startDt()),
                        endDtEquals(parameter.endDt())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(Wildcard.count)
                .from(user)
                .innerJoin(store).on(store.user.id.eq(user.id))
                .innerJoin(reservation).on(reservation.store.id.eq(store.id))
                .where(
                        typeEquals(parameter.type()),
                        statusEquals(parameter.status()),
                        startDtEquals(parameter.startDt()),
                        endDtEquals(parameter.endDt())
                ).fetchOne();

        return new PageImpl<>(results, pageable, totalCount);
    }

    @Override
    public Page<ReserveResponse.Infos> getUserReservations(Long userId, ReserveRequest.Parameter parameter, Pageable pageable) {

        List<ReserveResponse.Infos> results = queryFactory
                .select(Projections.constructor(ReserveResponse.Infos.class ,
                        user.id.as("userId") ,
                        store.id.as("storeId") ,
                        reservation.id.as("reservationId") ,
                        reservation.reservationNo ,
                        reservation.numberPeople ,
                        reservation.reservationDate ,
                        reservation.reservationTime ,
                        reservation.type ,
                        reservation.status))
                .from(user)
                .innerJoin(reservation).on(reservation.user.id.eq(user.id))
                .innerJoin(reservation.store , store)
                .where(
                        user.id.eq(userId),
                        typeEquals(parameter.type()),
                        statusEquals(parameter.status()),
                        startDtEquals(parameter.startDt()),
                        endDtEquals(parameter.endDt())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(Wildcard.count)
                .from(user)
                .innerJoin(reservation).on(reservation.user.id.eq(user.id))
                .innerJoin(reservation.store , store)
                .where(
                        typeEquals(parameter.type()),
                        statusEquals(parameter.status()),
                        startDtEquals(parameter.startDt()),
                        endDtEquals(parameter.endDt())
                ).fetchOne();

        return new PageImpl<>(results, pageable, totalCount);
    }

    @Override
    public ReserveResponse.Info getReservation(Long userId , Long reservationId) {

        QReservation r2 = new QReservation("r2");
        JPQLQuery<Long> subquery = JPAExpressions
                .select(r2.count())
                .from(r2)
                .where(r2.reservationNo.lt(reservation.reservationNo),
                        r2.type.eq(ReservationType.WAIT),
                        r2.status.in(ReservationStatus.APPLY),
                        r2.reservationDate.eq(LocalDate.now()),
                        r2.reservationTime.lt(reservation.reservationTime));

        return queryFactory
                .select(Projections.constructor(ReserveResponse.Info.class ,
                        user.id,
                        store.id,
                        reservation.id,
                        reservation.reservationNo,
                        reservation.numberPeople,
                        subquery ,
                        reservation.reservationDate,
                        reservation.reservationTime,
                        reservation.type,
                        reservation.status))
                .from(user)
                .innerJoin(reservation).on(reservation.user.id.eq(user.id))
                .innerJoin(reservation.store , store)
                .where(user.id.eq(userId) , reservation.id.eq(reservationId))
                .fetchOne();
    }

    private BooleanExpression typeEquals(ReservationType type) {
        return type != null ? reservation.type.eq(type) : null;
    }

    private BooleanExpression statusEquals(ReservationStatus status) {
        return status != null ? reservation.status.eq(status) : null;
    }

    private BooleanExpression startDtEquals(LocalDate startDt) {
        return startDt != null ? reservation.reservationDate.goe(startDt) : null;
    }

    private BooleanExpression endDtEquals(LocalDate endDt) {
        return endDt != null ? reservation.reservationDate.loe(endDt) : null;
    }
}
