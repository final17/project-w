package com.projectw.common.exceptions;

import com.projectw.common.enums.ResponseCode;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends ApiException{

    public InvalidTokenException() {
        super(HttpStatus.UNAUTHORIZED, ResponseCode.INVALID_TOKEN.getMessage());
    }
}
