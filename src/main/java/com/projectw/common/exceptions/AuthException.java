package com.projectw.common.exceptions;

import com.projectw.common.enums.ResponseCode;
import org.springframework.http.HttpStatus;

public class AuthException extends ApiException{

    public AuthException(ResponseCode code) {
        super(HttpStatus.UNAUTHORIZED, code.getMessage());
    }
}
