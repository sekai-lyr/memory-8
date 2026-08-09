package com.example.wordlearning.config;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.ResultCode;
import com.example.wordlearning.exception.PremiumRequiredException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("；"));
        return ApiResponse.fail(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBadRequest(IllegalArgumentException ex) {
        return ApiResponse.fail(ResultCode.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleState(IllegalStateException ex) {
        return ApiResponse.fail(ResultCode.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException ex) {
        return ApiResponse.fail(ResultCode.UNAUTHORIZED, "Unauthorized");
    }

    @ExceptionHandler(PremiumRequiredException.class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public ApiResponse<Void> handlePremiumRequired(PremiumRequiredException ex) {
        return ApiResponse.fail(ResultCode.PREMIUM_REQUIRED, ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoResource(NoResourceFoundException ex) {
        return ApiResponse.fail(ResultCode.NOT_FOUND, "Resource not found");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception ex) {
        return ApiResponse.fail(ResultCode.ERROR, ex.getMessage());
    }
}
