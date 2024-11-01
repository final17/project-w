package com.projectw.common.exceptions;

import com.projectw.common.enums.ResponseCode;
import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends ApiException {
    public PaymentNotFoundException(ResponseCode code) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, code.getMessage());
    }
}
