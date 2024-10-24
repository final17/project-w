package com.projectw.domain.allergy.repository;

import com.projectw.domain.allergy.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long> {
}
