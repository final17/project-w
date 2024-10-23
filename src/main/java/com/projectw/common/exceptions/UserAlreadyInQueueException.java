package com.projectw.common.exceptions;

import com.projectw.common.enums.ResponseCode;
import org.springframework.http.HttpStatus;

public class UserAlreadyInQueueException extends ApiException{

    public UserAlreadyInQueueException(ResponseCode code) {
        super(HttpStatus.CONFLICT, code.getMessage());
    }
}
