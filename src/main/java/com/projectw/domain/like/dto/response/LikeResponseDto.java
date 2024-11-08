package com.projectw.domain.like.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponseDto {
    private boolean liked;    // 현재 유저가 좋아요를 눌렀는지 여부
    private long likeCount;   // 해당 엔티티(리뷰나 메뉴 등)의 총 좋아요 수
}