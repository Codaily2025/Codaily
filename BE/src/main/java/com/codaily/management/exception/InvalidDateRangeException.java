package com.codaily.management.exception;

import com.codaily.global.exception.BusinessException;
import com.codaily.global.exception.ErrorCode;

public class InvalidDateRangeException extends BusinessException {
    public InvalidDateRangeException() {
        super(ErrorCode.INVALID_DATE_RANGE);
    }

    public InvalidDateRangeException(String message) {
        super(ErrorCode.INVALID_DATE_RANGE, message);
    }
}
