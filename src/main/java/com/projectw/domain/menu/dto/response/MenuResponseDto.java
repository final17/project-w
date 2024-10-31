package com.projectw.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class MenuResponseDto {
    private Long id;
    private String name;
    private int price;
    private Set<String> allergies;
    private int likesCount;
    private int viewCount;
}
