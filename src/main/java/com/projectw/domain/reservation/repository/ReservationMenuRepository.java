package com.projectw.domain.reservation.repository;

import com.projectw.domain.reservation.entity.ReservationMenu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationMenuRepository extends JpaRepository<ReservationMenu, Long> {
}
