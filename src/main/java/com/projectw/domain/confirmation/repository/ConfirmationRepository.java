package com.projectw.domain.confirmation.repository;

import com.projectw.domain.confirmation.entity.Confirmation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfirmationRepository extends JpaRepository<Confirmation, Long> , ConfirmationDslRepository {
}
