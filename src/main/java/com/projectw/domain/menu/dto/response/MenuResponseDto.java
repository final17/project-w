package com.projectw.domain.menu.dto.response;

import java.util.Set;

public sealed interface MenuResponseDto permits MenuResponseDto.Detail, MenuResponseDto.Summary {

    record Detail(
            Long id,
            String name,
            int price,
            Set<String> allergies,
            long viewCount,
            String imageUrl
    ) implements MenuResponseDto { }

    record Summary(
            Long id,
            String name,
            int price,
            String imageUrl
    ) implements MenuResponseDto { }
}