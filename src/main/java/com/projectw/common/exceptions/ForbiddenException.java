package com.projectw.common.exceptions;

import com.projectw.common.enums.ResponseCode;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
    public ForbiddenException(ResponseCode responseCode) {
        super(HttpStatus.FORBIDDEN, responseCode.getMessage());
    }
}