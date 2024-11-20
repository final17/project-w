package com.projectw.domain.reservation.repository;


import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.entity.QReservationMenu;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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
import java.util.List;

import static com.projectw.domain.reservation.entity.QReservation.reservation;
import static com.projectw.domain.store.entity.QStore.store;
import static com.projectw.domain.user.entity.QUser.user;

@Slf4j
@RequiredArgsConstructor
public class ReservationDslRepositoryImpl implements ReservationDslRepository{

    private final JPAQueryFactory queryFactory;
    private final QReservationMenu reservationMenu = QReservationMenu.reservationMenu;  // 추가


    @Override
    public Page<ReserveResponse.Infos> getOwnerReservations(Long userId, Long storeId, ReserveRequest.OwnerParameter parameter, Pageable pageable) {
        JPQLQuery<Long> menuIdSubQuery = JPAExpressions
                .select(reservationMenu.menu.id.min())  // QueryDSL의 min() 메서드 사용
                .from(reservationMenu)
                .where(reservationMenu.reservation.id.eq(reservation.id));

        List<ReserveResponse.Infos> results = queryFactory
                .select(Projections.constructor(ReserveResponse.Infos.class,
                        reservation.orderId,
                        reservation.user.id,
                        store.id,
                        menuIdSubQuery,
                        reservation.id,
                        reservation.reservationNo,
                        reservation.numberPeople,
                        reservation.paymentAmt,
                        reservation.paymentYN,
                        reservation.reservationDate,
                        reservation.reservationTime,
                        reservation.type,
                        reservation.status
                ))
                .from(store)
                .join(reservation).on(reservation.store.id.eq(store.id))
                .where(store.id.eq(storeId)
                        .and(typeEquals(parameter.type()))
                        .and(statusEquals(parameter.status()))
                        .and(startDtEquals(parameter.startDt()))
                        .and(endDtEquals(parameter.endDt())))
                .orderBy(reservation.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(Wildcard.count)
                .from(store)
                .innerJoin(reservation).on(reservation.store.id.eq(store.id))
                .where(
                        store.id.eq(parameter.storeId()),
                        typeEquals(parameter.type()),
                        statusEquals(parameter.status()),
                        startDtEquals(parameter.startDt()),
                        endDtEquals(parameter.endDt())
                ).fetchOne();

        return new PageImpl<>(results, pageable, totalCount);
    }

    @Override
    public Page<ReserveResponse.Infos> getReservations(Long userId, ReserveRequest.Parameter parameter, Pageable pageable) {

        JPQLQuery<Long> menuIdSubQuery = JPAExpressions
                .select(reservationMenu.menu.id.min())
                .from(reservationMenu)
                .where(reservationMenu.reservation.id.eq(reservation.id));

        List<ReserveResponse.Infos> results = queryFactory
                .select(Projections.constructor(ReserveResponse.Infos.class,
                        reservation.orderId,
                        user.id,
                        store.id,
                        menuIdSubQuery,
                        reservation.id,
                        reservation.reservationNo,
                        reservation.numberPeople,
                        reservation.paymentAmt,
                        reservation.paymentYN,
                        reservation.reservationDate,
                        reservation.reservationTime,
                        reservation.type,
                        reservation.status
                ))
                .from(user)
                .innerJoin(reservation).on(reservation.user.id.eq(user.id))
                .innerJoin(reservation.store, store)
                .where(
                        user.id.eq(userId),
                        typeEquals(parameter.type()),
                        statusEquals(parameter.status()),
                        startDtEquals(parameter.startDt()),
                        endDtEquals(parameter.endDt())
                )
                .orderBy(reservation.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(Wildcard.count)
                .from(user)
                .innerJoin(reservation).on(reservation.user.id.eq(user.id))  // results 쿼리와 동일한 join 조건
                .innerJoin(reservation.store, store)  // results 쿼리와 동일한 join
                .where(
                        user.id.eq(userId),
                        typeEquals(parameter.type()),     // parameter 사용
                        statusEquals(parameter.status()), // parameter 사용
                        startDtEquals(parameter.startDt()), // parameter 사용
                        endDtEquals(parameter.endDt())    // parameter 사용
                ).fetchOne();

        return new PageImpl<>(results, pageable, totalCount);
    }

//    @Override
//    public Page<ReserveResponse.Infos> getUserReservations(Long userId, ReserveRequest.Parameter parameter, Pageable pageable) {
//
//        JPQLQuery<Long> menuIdSubQuery = JPAExpressions
//                .select(reservationMenu.menu.id.min())
//                .from(reservationMenu)
//                .where(reservationMenu.reservation.id.eq(reservation.id));
//
//        List<ReserveResponse.Infos> results = queryFactory
//                .select(Projections.constructor(ReserveResponse.Infos.class,
//                        reservation.orderId,
//                        user.id,
//                        store.id,
//                        menuIdSubQuery,
//                        reservation.id,
//                        reservation.reservationNo,
//                        reservation.numberPeople,
//                        reservation.paymentAmt,
//                        reservation.paymentYN,
//                        reservation.reservationDate,
//                        reservation.reservationTime,
//                        reservation.type,
//                        reservation.status))
//                .from(user)
//                .innerJoin(reservation).on(reservation.user.id.eq(user.id))
//                .innerJoin(reservation.store, store)
//                .where(
//                        user.id.eq(userId),
//                        typeEquals(parameter.type()),
//                        statusEquals(parameter.status()),
//                        startDtEquals(parameter.startDt()),
//                        endDtEquals(parameter.endDt())
//                )
//                .orderBy(reservation.id.desc())
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        Long totalCount = queryFactory
//                .select(Wildcard.count)
//                .from(user)
//                .innerJoin(reservation).on(reservation.user.id.eq(user.id))
//                .innerJoin(reservation.store, store)
//                .where(
//                        user.id.eq(userId),
//                        typeEquals(parameter.type()),
//                        statusEquals(parameter.status()),
//                        startDtEquals(parameter.startDt()),
//                        endDtEquals(parameter.endDt())
//                ).fetchOne();
//
//        return new PageImpl<>(results, pageable, totalCount);
//    }

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
