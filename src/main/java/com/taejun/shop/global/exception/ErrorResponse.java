package com.taejun.shop.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String code,
        String message,
        String path,
        List<ValidationError> errors
) {

    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            String path
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                message,
                path,
                List.of()
        );
    }

    public static ErrorResponse of(
            ErrorCode errorCode,
            String path
    ) {
        return of(
                errorCode,
                errorCode.getMessage(),
                path
        );
    }

    public static ErrorResponse validation(
            List<FieldError> fieldErrors,
            String path
    ) {
        List<ValidationError> errors = fieldErrors.stream()
                .map(error -> new ValidationError(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()
                ))
                .toList();

        return new ErrorResponse(
                LocalDateTime.now(),
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                ErrorCode.INVALID_INPUT_VALUE.getMessage(),
                path,
                errors
        );
    }

    public record ValidationError(
            String field,
            Object rejectedValue,
            String message
    ) {
    }
}
