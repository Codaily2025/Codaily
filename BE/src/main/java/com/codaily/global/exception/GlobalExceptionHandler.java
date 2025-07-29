package com.codaily.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handlerBusinessException(
            BusinessException e,
            HttpServletRequest request){
        log.warn("Business exception occured: {} at {}", e.getMessage(), request.getRequestURI());

        ErrorResponse response = ErrorResponse.of(e.getErrorCode(), e.getMessage());
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    //날짜 파싱 오류
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParseException(
            DateTimeParseException e,
            HttpServletRequest request) {

        log.warn("Date parsing error: {} at {}", e.getMessage(), request.getRequestURI());

        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_DATE_FORMAT);
        response.setPath(request.getRequestURI());

        return ResponseEntity
                .status(ErrorCode.INVALID_DATE_FORMAT.getStatus())
                .body(response);
    }

    //파라미터 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {

        log.warn("Method argument type mismatch: {} at {}", e.getMessage(), request.getRequestURI());

        String message = String.format("'%s' 파라미터의 값 '%s'이(가) 올바르지 않습니다.",
                e.getName(), e.getValue());

        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, message);
        response.setPath(request.getRequestURI());

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(response);
    }

    //404
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException e,
            HttpServletRequest request) {

        log.warn("No handler found: {} {}", e.getHttpMethod(), e.getRequestURL());

        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("요청한 리소스를 찾을 수 없습니다.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    //예상치 못한 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request) {

        log.error("Unexpected error occurred at {}: ", request.getRequestURI(), e);

        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        response.setPath(request.getRequestURI());

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(response);
    }
}
