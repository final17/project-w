package com.projectw.domain.reservation.repository;

import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationDslRepository {
    boolean existsByUserIdAndStoreIdAndTypeAndStatus(Long user_id, Long store_id, ReservationType type, ReservationStatus status);

    @Query("SELECT IFNULL(MAX(r.reservationNo) + 1,1) FROM Reservation r WHERE r.type = :type AND r.reservationDate = :date")
    long findMaxReservationDate(@Param("type") ReservationType type , @Param("date") LocalDate date);

    @Query("SELECT r FROM Reservation r INNER JOIN FETCH r.store s INNER JOIN FETCH s.user u WHERE r.id = :id")
    Optional<Reservation> findReservationById(@Param("id") Long id);

    @Query("SELECT count(r) FROM Reservation r " +
            "WHERE r.type = :type " +
            "AND r.status NOT IN(:statuses) " +   // status에 여러 값
            "AND r.reservationDate = :date " +
            "AND r.reservationTime = :time")
    long countReservationByDate(@Param("type") ReservationType type,
                                @Param("statuses") List<ReservationStatus> statuses,
                                @Param("date") LocalDate date,
                                @Param("time") LocalTime time);
}
