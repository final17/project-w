package com.projectw.domain.payment.exception;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.ApiException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Getter
@Slf4j
public class InsufficientSeatsException extends ApiException {
    public InsufficientSeatsException(ResponseCode responseCode) {
        super(HttpStatus.BAD_REQUEST , responseCode.getMessage());
    }
}
