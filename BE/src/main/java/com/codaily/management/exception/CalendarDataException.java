package com.codaily.management.exception;

import com.codaily.global.exception.BusinessException;
import com.codaily.global.exception.ErrorCode;
import com.codaily.management.dto.CalendarResponse;

public class CalendarDataException extends BusinessException {
    public CalendarDataException(){
        super(ErrorCode.CALENDAR_DATA_ERROR);
    }

    public CalendarDataException(String message) {
        super(ErrorCode.CALENDAR_DATA_ERROR, message);
    }

    public CalendarDataException(String message, Throwable cause) {
        super(ErrorCode.CALENDAR_DATA_ERROR, message, cause);
    }
}
