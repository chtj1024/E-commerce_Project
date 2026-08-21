package com.taejun.shop.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException이 발생하면 호출
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(
            CustomException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        ErrorResponse response = ErrorResponse.of(
                errorCode,
                exception.getMessage(),
                request.getRequestURI()
        );


        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    // @Valid 검증 실패했을 때 호출
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.validation(
                exception.getBindingResult().getFieldErrors(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(response);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            BindException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidInputException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.warn(
                "잘못된 요청값입니다. path={}, exception={}",
                request.getRequestURI(),
                exception.getClass().getSimpleName()
        );

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "예상하지 못한 서버 오류가 발생했습니다. path={}",
                request.getRequestURI(),
                exception
        );

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(response);

    }

}
