package com.projectw.domain.dibs.repository;

import com.projectw.domain.dibs.entity.Dibs;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DibsRepository extends JpaRepository<Dibs, Long> {

    List<Dibs> findByUserId(Long userId);
    int deleteByUserAndStore(User user, Store store);
}
