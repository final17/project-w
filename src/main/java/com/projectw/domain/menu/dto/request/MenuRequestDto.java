package com.projectw.domain.menu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public sealed interface MenuRequestDto permits MenuRequestDto.Create, MenuRequestDto.Update {

    record Create(
            @NotBlank(message = "메뉴 이름은 필수입니다.")
            String name,

            @NotNull(message = "가격은 필수입니다.")
            @Min(value = 1, message = "가격은 1 이상이어야 합니다.")
            int price,

            List<Long> allergyIds,

            MultipartFile image
    ) implements MenuRequestDto { }

    record Update(
            @NotNull(message = "메뉴 ID는 필수입니다.")
            Long id,

            String name,

            @Min(value = 1, message = "가격은 1 이상이어야 합니다.")
            Integer price,

            List<Long> allergyIds,

            MultipartFile image
    ) implements MenuRequestDto { }
}