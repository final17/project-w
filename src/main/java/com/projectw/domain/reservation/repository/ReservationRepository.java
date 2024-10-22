package com.projectw.domain.reservation.repository;

import com.projectw.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationDslRepository {

}
