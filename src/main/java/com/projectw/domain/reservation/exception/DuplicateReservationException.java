package com.projectw.domain.reservation.exception;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.ApiException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Getter
@Slf4j
public class DuplicateReservationException extends ApiException {
    public DuplicateReservationException(ResponseCode responseCode) {
        super(HttpStatus.CONFLICT , responseCode.getMessage());
    }
}
