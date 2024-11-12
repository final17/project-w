package com.projectw.domain.like.dto;

public sealed interface LikeResponseDto permits LikeResponseDto.Basic {

    record Basic(boolean liked, long likeCount) implements LikeResponseDto { }
}