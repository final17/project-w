package com.projectw.common.exceptions;

import org.springframework.http.HttpStatus;

public class RedisLocakTimeoutException extends ApiException {
    public RedisLocakTimeoutException() {
        super(HttpStatus.REQUEST_TIMEOUT, "레디스 락 타임아웃");
    }
}
