package com.projectw.domain.menu.repository;

import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 특정 가게의 모든 메뉴 조회
    List<Menu> findAllByStore(Store store);
}
