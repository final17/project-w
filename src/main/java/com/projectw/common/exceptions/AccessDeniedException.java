package com.projectw.common.exceptions;

import com.projectw.common.enums.ResponseCode;
import org.springframework.http.HttpStatus;

/**
 * 사용자가 인증되었으나 접근 권한이 없을 때 사용합니다.
 * ex) 다른 사람의 게시글을 삭제하거나, 어드민 페이지에 접근하거나
 */
public class AccessDeniedException extends ApiException{
    public AccessDeniedException(ResponseCode code)
    {
        super(HttpStatus.FORBIDDEN, code.getMessage());
    }
}
