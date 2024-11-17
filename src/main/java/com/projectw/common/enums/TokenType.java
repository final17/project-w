package com.projectw.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TokenType {
    ACCESS(15 * 60 * 1000), // 15 min
    REFRESH(24 * 60 * 60* 1000); // 24hour
    //ACCESS(10* 60* 1000), // 1sec
    //REFRESH(30 * 60 * 1000); // 15sec

    private final long lifeTime;
}
