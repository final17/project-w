package com.projectw.domain.reservation.exception;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidCartException extends ApiException {
    public InvalidCartException(ResponseCode responseCode) {
        super(HttpStatus.BAD_REQUEST , responseCode.getMessage());
    }
}
