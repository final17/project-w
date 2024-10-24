package com.projectw.domain.allergy.repository;

import com.projectw.domain.allergy.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long> {
    Set<Allergy> findAllById(List<Long> ids);
}
