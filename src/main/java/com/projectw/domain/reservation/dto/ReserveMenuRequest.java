package com.projectw.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;

public sealed interface ReserveMenuRequest permits ReserveMenuRequest.Menu {
    record Menu(
            @NotNull(message = "메뉴 식별키는 필수입니다.")
            Long menuId,
            @NotNull(message = "메뉴 개수는 필수입니다.")
            Long menuCnt
    ) implements ReserveMenuRequest {}
}
