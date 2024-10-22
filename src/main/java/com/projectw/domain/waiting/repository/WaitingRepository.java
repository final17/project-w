package com.projectw.domain.waiting.repository;

import com.projectw.domain.waiting.entity.Waiting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitingRepository extends JpaRepository<Waiting, Long>
        , WaitingDslRepository {

}
