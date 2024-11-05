package com.projectw.domain.reservation.dto;

public sealed interface ReserveRedis permits ReserveRedis.Menu {
    record Menu(
            Long menuId,
            String menuName,
            Long price,
            Long menuCnt
    ) implements ReserveRedis{
        public Menu updateCnt(Long cnt){
            return new Menu(menuId, menuName , price , cnt);
        }
    }
}
