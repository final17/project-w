package com.projectw.common.exceptions;

import com.projectw.common.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class NotFoundException extends ApiException {
    public NotFoundException(ResponseCode responseCode) {
        super(HttpStatus.NOT_FOUND, responseCode.getMessage());
    }
}