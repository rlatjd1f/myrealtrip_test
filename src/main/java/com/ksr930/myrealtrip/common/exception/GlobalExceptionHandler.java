package com.ksr930.myrealtrip.common.exception;

import com.ksr930.myrealtrip.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleApiException(ApiException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(ex.getErrorCode(), new ErrorDetail(List.of())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldViolation> fieldViolations = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toViolation)
                .toList();
        ErrorDetail detail = new ErrorDetail(fieldViolations);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST, detail));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleMissingParam(MissingServletRequestParameterException ex) {
        ErrorDetail detail = new ErrorDetail(List.of(
                new FieldViolation(ex.getParameterName(), "required parameter is missing")));
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST, detail));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorDetail detail = new ErrorDetail(List.of(
                new FieldViolation(ex.getName(), "invalid parameter type")));
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST, detail));
    }

    private FieldViolation toViolation(FieldError error) {
        return new FieldViolation(error.getField(), error.getDefaultMessage());
    }
}
