package com.projectw.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class MenuResponseDto {
    private Long id;
    private String name;
    private int price;
    private Set<String> allergies;
    private long viewCount;
}