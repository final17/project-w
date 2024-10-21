package com.projectw.common.exceptions;

import com.projectw.common.enums.ResponseCode;
import org.springframework.http.HttpStatus;

public class InvalidRequestException extends ApiException {
    public InvalidRequestException(ResponseCode code) {
        super(HttpStatus.BAD_REQUEST, code.getMessage());
    }
}
